package adudecalledleo.entityevents.impl;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.FabricMixinTransformerProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@ApiStatus.Internal
public class PreLaunchInitializer implements PreLaunchEntrypoint {
    public static final Logger LOGGER = LogManager.getLogger("Entity Events|PreLaunch");

    private static final Field ACTIVE_TRANSFORMER, MIXIN_TRANSFORMER;
    static {
        try {
            ACTIVE_TRANSFORMER = MixinEnvironment.class.getDeclaredField("transformer");
            ACTIVE_TRANSFORMER.setAccessible(true);
            MIXIN_TRANSFORMER = Class.forName("net.fabricmc.loader.launch.knot.KnotClassDelegate").getDeclaredField("mixinTransformer");
            MIXIN_TRANSFORMER.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static class FabricMixinTransformerProxyProxy extends FabricMixinTransformerProxy {
        private final FabricMixinTransformerProxy proxy;

        public FabricMixinTransformerProxyProxy(FabricMixinTransformerProxy proxy) {
            this.proxy = proxy;
        }

        @Override
        public byte[] transformClassBytes(String name, String transformedName, byte[] basicClass) {
            byte[] transformed = this.proxy.transformClassBytes(name, transformedName, basicClass);
            if (name.equals("org.objectweb.asm.ClassReader"))
                // hardcoded exception to prevent infinite loops
                return transformed;
            transformed = tryInjectCallback(transformed);
            return transformed;
        }
    }

    public static final ObjectOpenHashSet<String> ENTITY_SUBCLASSES = new ObjectOpenHashSet<>();

    public static String rtEntity, rtDamageSource;
    public static String rtDamageName, rtDamageDesc, rtInvokeDamageDesc;
    public static String rtTickName, rtInvokeTickDesc;

    private static byte[] tryInjectCallback(final byte[] original) {
        if (original == null)
            return null;

        ClassReader classReader = new ClassReader(original);

        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, Opcodes.ASM9);

        if (!ENTITY_SUBCLASSES.contains(classNode.name)) {
            if (classNode.superName != null) {
                // transform superclass first (because for some reason we get them in reverse by default)
                try {
                    PreLaunchInitializer.class.getClassLoader().loadClass(classNode.superName.replace('/', '.'));
                } catch (ClassNotFoundException ignored) { }
                if (ENTITY_SUBCLASSES.contains(classNode.superName))
                    ENTITY_SUBCLASSES.add(classNode.name);
                else
                    return original;
            } else
                return original;
        }

        LOGGER.info("Transforming class '{}'!", classNode.name);

        boolean modified = false;
        for (MethodNode method : classNode.methods) {
            if ((method.access & Opcodes.ACC_PUBLIC) == 0)
                // method must be public
                continue;
            if (rtDamageName.equals(method.name) && rtDamageDesc.equals(method.desc)) {
                method.instructions.insert(getDamageInjectionInstructions());
                modified = true;
            } else if (rtTickName.equals(method.name) && "()V".equals(method.desc)) {
                method.instructions.insert(getTickInjectionInstructions());
                modified = true;
            }
        }

        if (!modified)
            return original;

        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    @SuppressWarnings("CommentedOutCode")
    private static InsnList getDamageInjectionInstructions() {
        final InsnList instructions = new InsnList();

        // inject the following code block into the top of the method:
        /*
        if (EntityDamageEventsInternals.invoke(this, source, amount))
            return false;
         */

        final LabelNode continueLabel = new LabelNode();

        // load up ze locals
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1)); // source
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 2)); // amount
        // invoke our damage events (if they exist) via EntityDamageEventsInternals.invoke
        // note that this'll just return false if this entity already tried to invoke this tick,
        // meaning every entity will only invoke *once* per tick, no matter how many superclasses deep it is
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "adudecalledleo/entityevents/impl/EntityDamageEventsInternals",
                "invoke",
                rtInvokeDamageDesc));
        instructions.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
        // if we should cancel, return false
        instructions.add(new InsnNode(Opcodes.ICONST_0));
        instructions.add(new InsnNode(Opcodes.IRETURN));
        // else, continue with the rest of the method
        instructions.add(continueLabel);

        return instructions;
    }

    @SuppressWarnings("CommentedOutCode")
    private static InsnList getTickInjectionInstructions() {
        final InsnList instructions = new InsnList();

        // inject the following code block into the top of the method:
        /*
        if (EntityTickEventsInternals.invoke(this))
            return;
         */

        final LabelNode continueLabel = new LabelNode();

        // load up our singular local
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
        // invoke our tick events (if they exist) via EntityTickEventsInternals.invoke
        // note that this'll just return false if this entity already tried to invoke this tick,
        // meaning every entity will only invoke *once* per tick, no matter how many superclasses deep it is
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "adudecalledleo/entityevents/impl/EntityTickEventsInternals",
                "invoke",
                rtInvokeTickDesc));
        instructions.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
        // if we should cancel, return
        instructions.add(new InsnNode(Opcodes.RETURN));
        // else, continue with the rest of the method
        instructions.add(continueLabel);

        return instructions;
    }

    @Override
    public void onPreLaunch() {
        LOGGER.info("Oh god what have I done [Entity Events pregaming!]");

        // get runtime names
        MappingResolver map = FabricLoader.getInstance().getMappingResolver();

        rtEntity = map.mapClassName("intermediary", "net.minecraft.class_1297");
        rtDamageSource = map.mapClassName("intermediary", "net.minecraft.class_1282");
        rtDamageName = map.mapMethodName("intermediary", "net.minecraft.class_1297",
                "method_5643", "(Lnet/minecraft/class_1297;Lnet/minecraft/class_1282;F)Z");
        rtDamageDesc = String.format("(L%s;F)Z", rtDamageSource);
        rtInvokeDamageDesc = String.format("(L%s;L%s;F)Z", rtEntity, rtDamageSource);
        rtTickName = map.mapMethodName("intermediary", "net.minecraft.class_1297",
                "method_18756", "()V");
        rtInvokeTickDesc = String.format("(L%s;)Z", rtEntity);

        ENTITY_SUBCLASSES.add(rtEntity.replace('.', '/'));

        try {
            // first we need knot's delegate
            Object delegate = getDelegate();
            // then we get the current mixin transformer
            FabricMixinTransformerProxy proxy = (FabricMixinTransformerProxy) MIXIN_TRANSFORMER.get(delegate);

            Field active = FabricMixinTransformerProxy.class.getDeclaredField("transformer");
            active.setAccessible(true);

            // we need to delete mixin's current mixin transformer to get around it's constructor check
            Object currentTransformer = active.get(proxy);

            // we remove the active transformer
            ACTIVE_TRANSFORMER.set(null, null);
            // this action overrides the current transformer
            FabricMixinTransformerProxyProxy proxyProxy = new FabricMixinTransformerProxyProxy(proxy);
            // so we return the old one
            ACTIVE_TRANSFORMER.set(null, currentTransformer);

            MIXIN_TRANSFORMER.set(delegate, proxyProxy);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to install mass ASM proxy", e);
        }
    }

    public Object getDelegate() throws ReflectiveOperationException {
        ClassLoader loader = PreLaunchInitializer.class.getClassLoader();
        Class<?> knotInterface = Class.forName("net.fabricmc.loader.launch.knot.KnotClassLoaderInterface");
        while (loader != null && !knotInterface.isInstance(loader))
            loader = loader.getParent();

        Method method = knotInterface.getMethod("getDelegate");
        method.setAccessible(true);
        return method.invoke(loader);
    }
}
