package adudecalledleo.entitydamageevents.impl;

import com.chocohead.mm.api.ClassTinkerers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;

public class CallbackInjector implements Runnable {
    public static final Logger LOGGER = LogManager.getLogger("Entity Damage Events|CallbackInjector");

    @Override
    public void run() {
        LOGGER.info("Early bird gets the cursed worm (callback injector running!)");

        // TODO obtain list of all Entity classes that override the damage method
        // for now let's just hardcode some classes for testing
        ArrayList<String> entityClasses = new ArrayList<>();
        entityClasses.add("net.minecraft.entity.Entity");
        entityClasses.add("net.minecraft.entity.LivingEntity");
        entityClasses.add("net.minecraft.entity.mob.HostileEntity");
        entityClasses.add("net.minecraft.entity.mob.ZombieEntity");

        for (String entityClass : entityClasses) {
            LOGGER.info("Selected {}.damage for injection", entityClass);
            ClassTinkerers.addTransformation(entityClass, this::injectCallback);
        }
    }

    // this injects the following code block into the top of the "damage" method:
    /*
    if (EntityDamageEventInternals.invoke(this, source, amount))
        return false;
     */
    private void injectCallback(ClassNode classNode) {
        LOGGER.info("Now injecting callback into {}.damage", classNode.name);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("damage") || method.name.equals("method_5643")) {
                LOGGER.info("Located damage method!");

                if (method.maxStack < 3) {
                    LOGGER.info("maxStack < 3, setting maxStack to 3 so we get #verified");
                    method.maxStack = 3;
                }

                final AbstractInsnNode firstInsn = method.instructions.getFirst();

                LabelNode continueLabel = new LabelNode();

                InsnList insnList = new InsnList();

                // load up ze locals
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 1)); // source
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 2)); // amount
                // invoke our damage event (if it exists) via EntityDamageEventInternals.invoke
                // note that this'll just return false if this entity already tried to invoke this tick,
                // meaning every entity will only invoke *once* per tick, no matter how many superclasses deep it is
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "adudecalledleo/entitydamageevents/impl/EntityDamageEventInternals",
                        "invoke",
                        "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;F)Z"));
                insnList.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
                // if we should cancel, return false
                insnList.add(new InsnNode(Opcodes.ICONST_0));
                insnList.add(new InsnNode(Opcodes.IRETURN));
                // else, continue with the rest of the method
                insnList.add(continueLabel);

                method.instructions.insertBefore(firstInsn, insnList);

                LOGGER.info("Injection successful");

                return;
            }
        }
        LOGGER.info("uh, {} didn't have a damage method", classNode.name);
    }
}
