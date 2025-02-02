var ASMAPI = getJavaType('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = getJavaType('org.objectweb.asm.Opcodes');
var Label = getJavaType('org.objectweb.asm.Label');

var SHOW_DEBUG_MESSAGES = false;

var ImageBufferDownloadClassName = null;
var DownloadingTexturePatched = false;

// noinspection JSUnusedGlobalSymbols
function initializeCoreMod() {
    return {
        // --- feature: fix for multiplayer on 1.16.X
        'Multiplayer_MinecraftClientTransformer': createMethodsTransformer(
            'net.minecraft.client.Minecraft',
            {
                'func_238216_r_ ()Z': Minecraft_isMultiplayerOrChatEnabled,
                'func_238217_s_ ()Z': Minecraft_isMultiplayerOrChatEnabled
            }
        ),

        // --- feature: skin/cape textures from EasyX
        'Textures_DownloadingTextureTransformer': createMethodsTransformer(
            'net.minecraft.client.renderer.texture.DownloadingTexture',
            {
                'func_229163_c_ (Lnet/minecraft/client/renderer/texture/NativeImage;)Lnet/minecraft/client/renderer/texture/NativeImage;': DownloadingTexture_parseUserSkin
            }
        ),
        'Textures_DownloadImageBufferTransformer': createClassMethodsTransformer(
            'net.minecraft.client.renderer.DownloadImageBuffer',
            ImageBufferDownload$class,
            {
                '<init> ()V': ImageBufferDownload$init,
                'func_195786_a (Lnet/minecraft/client/renderer/texture/NativeImage;)Lnet/minecraft/client/renderer/texture/NativeImage;': ImageBufferDownload_parseUserSkin
            }
        ),
        'Textures_ImageBufferDownloadTransformer': createClassMethodsTransformer(
            'net.minecraft.client.renderer.ImageBufferDownload',
            ImageBufferDownload$class,
            {
                '<init> ()V': ImageBufferDownload$init,
                'func_195786_a (Lnet/minecraft/client/renderer/texture/NativeImage;)Lnet/minecraft/client/renderer/texture/NativeImage;': ImageBufferDownload_parseUserSkin
            }
        ),
        'Textures_SkinManagerTransformer': createMethodsTransformer(
            'net.minecraft.client.resources.SkinManager',
            {
                'func_152789_a (Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)Lnet/minecraft/util/ResourceLocation;': SkinManager_loadSkin,
                'func_152789_a (Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$ISkinAvailableCallback;)Lnet/minecraft/util/ResourceLocation;': SkinManager_loadSkin,
                'func_210275_a (Lcom/mojang/authlib/GameProfile;ZLnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)V': SkinManager_loadProfileTextures,
                'func_210275_a (Lcom/mojang/authlib/GameProfile;ZLnet/minecraft/client/resources/SkinManager$ISkinAvailableCallback;)V': SkinManager_loadProfileTextures,
                'func_229293_a_ (Lcom/mojang/authlib/GameProfile;ZLnet/minecraft/client/resources/SkinManager$ISkinAvailableCallback;)V': SkinManager_loadProfileTextures,
                'func_152788_a (Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;': SkinManager_loadSkinFromCache
            }
        ),
        'Textures_SkinManager$2Transformer': createMethodsTransformer(
            'net.minecraft.client.resources.SkinManager$2',
            {
                'func_195786_a (Lnet/minecraft/client/renderer/texture/NativeImage;)Lnet/minecraft/client/renderer/texture/NativeImage;': SkinManager$2_parseUserSkin
            }
        )
    }
}

function DownloadingTexture_parseUserSkin(methodNode) {
    var instructions = methodNode.instructions;
    var entry = null;

    for (var insnIndex = 0, insnCount = instructions.size(); insnIndex < insnCount; insnIndex++) {
        var insnNode = instructions.get(insnIndex);

        if (insnNode.getOpcode() === Opcodes.ARETURN && checkVarInsn(insnNode.getPrevious(), Opcodes.ALOAD, 0)) {
            entry = instructions.get(insnIndex - 3);
            break;
        }
    }

    if (entry === null)
        return false;

    // --- PATCH #1

    // 1. Load NativeImage instance, call getWidth() and getHeight()
    // 2. Compute scaleFactor, put it as a local variable
    // 3. Jump to return instruction if scaleFactor == 0
    var label = new Label();
    var node = ASMAPI.getMethodNode();
    node.visitLabel(label);
    node.visitVarInsn(Opcodes.ALOAD, 0);
    node.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'net/minecraft/client/renderer/texture/NativeImage', mapName('func_195702_a'), '()I');
    node.visitVarInsn(Opcodes.ALOAD, 0);
    node.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'net/minecraft/client/renderer/texture/NativeImage', mapName('func_195714_b'), '()I');
    node.visitMethodInsn(Opcodes.INVOKESTATIC, 'org/easylauncher/mods/elfeatures/textures/TexturesInspector', 'computeTextureScale', '(II)I', false);
    node.visitVarInsn(Opcodes.ISTORE, 3);
    node.visitVarInsn(Opcodes.ILOAD, 3);
    node.visitJumpInsn(Opcodes.IFEQ, getSkipInsn(entry));
    instructions.insert(node.instructions);

    // --- PATCH #2

    // replace ICONST_4 to BIPUSH 4
    for (var iterator = instructions.iterator(); iterator.hasNext();) {
        var insnNode = iterator.next();

        if (insnNode.getOpcode() === Opcodes.ICONST_4) {
            var node = ASMAPI.getMethodNode();
            node.visitIntInsn(Opcodes.BIPUSH, 4);
            instructions.set(insnNode, node.instructions.get(0));
        }
    }

    // multiply BIPUSH 4 with scaleFactor
    for (var iterator = instructions.iterator(); iterator.hasNext();) {
        var insnNode = iterator.next();

        if (insnNode.getOpcode() === Opcodes.BIPUSH) {
            var node = ASMAPI.getMethodNode();
            node.visitVarInsn(Opcodes.ILOAD, 3);
            node.visitInsn(Opcodes.IMUL);
            instructions.insert(insnNode, node.instructions);
        }
    }

    DownloadingTexturePatched = true;
    return true;
}

function ImageBufferDownload$class(classNode) {
    // add scale factor field
    classNode.visitField(Opcodes.ACC_PUBLIC, 'elfeatures$scaleFactor', 'I', null, null).visitEnd();
    return true;
}

function ImageBufferDownload$init(methodNode) {
    var instructions = methodNode.instructions;
    var entry = null;

    for (var iterator = instructions.iterator(); iterator.hasNext();) {
        var insnNode = iterator.next();

        if (insnNode.getOpcode() === Opcodes.RETURN) {
            entry = insnNode;
            break;
        }
    }

    if (entry === null)
        return false;

    // --- PATCH

    // this.elfeatures$scaleFactor = 1;
    var node = ASMAPI.getMethodNode();
    node.visitVarInsn(Opcodes.ALOAD, 0);
    node.visitInsn(Opcodes.ICONST_2);
    node.visitFieldInsn(Opcodes.PUTFIELD, ImageBufferDownloadClassName, "elfeatures$scaleFactor", "I");
    instructions.insertBefore(insnNode, node.instructions);

    return true;
}

function ImageBufferDownload_parseUserSkin(methodNode) {
    var instructions = methodNode.instructions;

    // replace ICONST_4 to BIPUSH 4
    for (var iterator = instructions.iterator(); iterator.hasNext();) {
        var insnNode = iterator.next();

        if (insnNode.getOpcode() === Opcodes.ICONST_4) {
            var node = ASMAPI.getMethodNode();
            node.visitIntInsn(Opcodes.BIPUSH, 4);
            instructions.set(insnNode, node.instructions.get(0));
        }
    }

    // multiply BIPUSH 4 with scaleFactor
    for (var iterator = instructions.iterator(); iterator.hasNext();) {
        var insnNode = iterator.next();

        if (insnNode.getOpcode() === Opcodes.BIPUSH) {
            var node = ASMAPI.getMethodNode();
            node.visitVarInsn(Opcodes.ALOAD, 0);
            node.visitFieldInsn(Opcodes.GETFIELD, ImageBufferDownloadClassName, "elfeatures$scaleFactor", "I");
            node.visitInsn(Opcodes.IMUL);
            instructions.insert(insnNode, node.instructions);
        }
    }

    return true;
}

function Minecraft_isMultiplayerOrChatEnabled(methodNode) {
    var instructions = methodNode.instructions;
    var entry = null;

    for (var iterator = instructions.iterator(); iterator.hasNext();) {
        var insnNode = iterator.next();

        if (insnNode.getOpcode() === Opcodes.IRETURN) {
            entry = insnNode;
            break;
        }
    }

    if (entry === null)
        return false;

    // --- PATCH

    // return true;
    var node = ASMAPI.getMethodNode();
    node.visitInsn(Opcodes.POP);
    node.visitInsn(Opcodes.ICONST_1);
    instructions.insertBefore(insnNode, node.instructions);

    return true;
}

function SkinManager_loadSkin(methodNode) {
    var instructions = methodNode.instructions;

    for (var insnIndex = 0, insnCount = instructions.size(); insnIndex < insnCount; insnIndex++) {
        var insnNode = instructions.get(insnIndex);

        if (checkFieldInsn(insnNode, Opcodes.GETSTATIC, 'SKIN Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;')
            && checkOpcodesChain(instructions, insnIndex + 1, [Opcodes.IF_ACMPNE, Opcodes.NEW, Opcodes.DUP, Opcodes.INVOKESPECIAL])
        ) {
            ImageBufferDownloadClassName = instructions.get(insnIndex + 2).desc;
            log("Found renamed 'ImageBufferDownload': " + ImageBufferDownloadClassName);
            return true;
        }
    }

    return false;
}

function SkinManager_loadProfileTextures(methodNode) {
    var instructions = methodNode.instructions;
    var entry = null;

    for (var insnIndex = 0, insnCount = instructions.size(); insnIndex < insnCount; insnIndex++) {
        var insnNode = instructions.get(insnIndex);

        if (checkMethodInsn(insnNode, Opcodes.INVOKESTATIC, 'func_71410_x ()Lnet/minecraft/client/Minecraft;')
            && checkOpcodesChain(instructions, insnIndex + 1, [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ALOAD])
        ) {
            entry = insnNode;
            break;
        }
    }

    if (entry === null)
        return false;

    // --- PATCH

    // #2 = ELFeaturesMod.texturesProvider().loadTextures(#1, #2);
    var node = ASMAPI.getMethodNode();
    node.visitMethodInsn(Opcodes.INVOKESTATIC, "org/easylauncher/mods/elfeatures/ELFeaturesMod", "texturesProvider", "()Lorg/easylauncher/mods/elfeatures/textures/TexturesProvider;");
    node.visitVarInsn(Opcodes.ALOAD, 1);
    node.visitVarInsn(Opcodes.ALOAD, 4);
    node.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/easylauncher/mods/elfeatures/textures/TexturesProvider", "loadTexturesMap", "(Lcom/mojang/authlib/GameProfile;Ljava/util/Map;)Ljava/util/Map;");
    node.visitVarInsn(Opcodes.ASTORE, 4);
    instructions.insertBefore(entry, node.instructions);

    return true;
}

function SkinManager_loadSkinFromCache(methodNode) {
    var instructions = methodNode.instructions;
    var entry = [null, null];
    var usesProperty = false;
    var entryV2 = null;

    for (var insnIndex = 0, insnCount = instructions.size(); insnIndex < insnCount; insnIndex++) {
        var insnNode = instructions.get(insnIndex);

        if (!usesProperty
            && checkMethodInsn(insnNode, Opcodes.INVOKEVIRTUAL, 'getProperties ()Lcom/mojang/authlib/properties/PropertyMap;')
            && checkOpcodesChain(instructions, insnIndex + 1, [Opcodes.LDC, Opcodes.INVOKEVIRTUAL, Opcodes.ACONST_NULL, Opcodes.INVOKESTATIC, Opcodes.CHECKCAST, Opcodes.ASTORE])
        ) {
            log("Detected usage of game profile properties instead of textures map directly (1.16.2 and newer)!");
            usesProperty = true;
            continue;
        }

        if (usesProperty && entryV2 === null
            && insnNode.getOpcode() === Opcodes.IFNONNULL
            && checkVarInsn(insnNode.getPrevious(), Opcodes.ALOAD, 2)
        ) {
            entryV2 = insnNode;
            continue;
        }

        if (checkMethodInsn(insnNode, Opcodes.INVOKEINTERFACE, 'getUnchecked (Ljava/lang/Object;)Ljava/lang/Object;')
            && checkOpcodesChain(instructions, insnIndex - (usesProperty ? 4 : 3), [Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ALOAD])
        ) {
            entry[0] = instructions.get(insnIndex - (usesProperty ? 4 : 3));
            entry[1] = insnNode;
            break;
        }
    }

    if (!validateEntryPoints(entry))
        return false;

    if (usesProperty) {
        if (entryV2 === null)
            return;

        // --- PATCH V2

        var node = ASMAPI.getMethodNode();
        node.visitLabel(new Label());
        node.visitMethodInsn(Opcodes.INVOKESTATIC, "org/easylauncher/mods/elfeatures/ELFeaturesMod", "texturesProvider", "()Lorg/easylauncher/mods/elfeatures/textures/TexturesProvider;");
        node.visitVarInsn(Opcodes.ALOAD, 1);
        node.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/easylauncher/mods/elfeatures/textures/TexturesProvider", "loadTexturesProperty", "(Lcom/mojang/authlib/GameProfile;)Lcom/mojang/authlib/properties/Property;");
        node.visitVarInsn(Opcodes.ASTORE, 2);
        node.visitLabel(new Label());
        node.visitVarInsn(Opcodes.ALOAD, 2);
        node.visitJumpInsn(Opcodes.IFNONNULL, getSkipInsn(entryV2.label));
        instructions.insert(entryV2, node.instructions);
    }

    // --- PATCH #1

    // ELFeaturesMod.texturesProvider() ... .loadTextures(#1, ...);
    var node = ASMAPI.getMethodNode();
    node.visitMethodInsn(Opcodes.INVOKESTATIC, "org/easylauncher/mods/elfeatures/ELFeaturesMod", "texturesProvider", "()Lorg/easylauncher/mods/elfeatures/textures/TexturesProvider;");
    node.visitVarInsn(Opcodes.ALOAD, 1);
    instructions.insertBefore(entry[0], node.instructions);

    // --- PATCH #2

    // ELFeaturesMod.texturesProvider().loadTextures(#1, #2);
    node = ASMAPI.getMethodNode();
    node.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/easylauncher/mods/elfeatures/textures/TexturesProvider", "loadTexturesMap", "(Lcom/mojang/authlib/GameProfile;Ljava/util/Map;)Ljava/util/Map;");
    instructions.insert(entry[1], node.instructions);

    return true;
}

function SkinManager$2_parseUserSkin(methodNode) {
    if (DownloadingTexturePatched) {
        log("DownloadingTexture has been patched, skipping this...");
        return true;
    }

    var instructions = methodNode.instructions;
    var entry = null;

    for (var insnIndex = 0, insnCount = instructions.size(); insnIndex < insnCount; insnIndex++) {
        var insnNode = instructions.get(insnIndex);

        if (checkFieldInsn(insnNode, Opcodes.GETFIELD, 'field_152635_a Lnet/minecraft/client/renderer/IImageBuffer;')
            && checkOpcodesChain(instructions, insnIndex + 1, [Opcodes.IFNULL])
        ) {
            entry = instructions.get(insnIndex + 1);
            break;
        }
    }

    if (entry === null)
        return false;

    // --- PATCH

    // 1. Load ImageBufferDownload instance
    // 2. Load NativeImage instance, call getWidth() and getHeight()
    // 3. Compute scaleFactor, put it as a field value
    // 4. Load scaleFactor from the field
    // 5. Jump to return instruction if scaleFactor == 0
    var label = new Label();
    var node = ASMAPI.getMethodNode();
    node.visitLabel(label);
    node.visitVarInsn(Opcodes.ALOAD, 0);
    node.visitFieldInsn(Opcodes.GETFIELD, 'net/minecraft/client/resources/SkinManager$2', 'field_152635_a', 'Lnet/minecraft/client/renderer/IImageBuffer;');
    node.visitTypeInsn(Opcodes.CHECKCAST, ImageBufferDownloadClassName);
    node.visitVarInsn(Opcodes.ALOAD, 1);
    node.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'net/minecraft/client/renderer/texture/NativeImage', mapName('func_195702_a'), '()I');
    node.visitVarInsn(Opcodes.ALOAD, 1);
    node.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'net/minecraft/client/renderer/texture/NativeImage', mapName('func_195714_b'), '()I');
    node.visitMethodInsn(Opcodes.INVOKESTATIC, 'org/easylauncher/mods/elfeatures/textures/TexturesInspector', 'computeTextureScale', '(II)I', false);
    node.visitFieldInsn(Opcodes.PUTFIELD, ImageBufferDownloadClassName, 'elfeatures$scaleFactor', 'I');
    node.visitVarInsn(Opcodes.ALOAD, 0);
    node.visitFieldInsn(Opcodes.GETFIELD, 'net/minecraft/client/resources/SkinManager$2', 'field_152635_a', 'Lnet/minecraft/client/renderer/IImageBuffer;');
    node.visitTypeInsn(Opcodes.CHECKCAST, ImageBufferDownloadClassName);
    node.visitFieldInsn(Opcodes.GETFIELD, ImageBufferDownloadClassName, 'elfeatures$scaleFactor', 'I');
    node.visitJumpInsn(Opcodes.IFEQ, getSkipInsn(entry.label));
    instructions.insert(entry, node.instructions);

    return true;
}

// --- transformers declaration routine

function createMethodsTransformer(className, methodTransformers) {
    return {
        'target': {'type': 'CLASS', 'name': className.replace('.', '/')},
        'transformer': function (classNode) {
            transformMethods(className, classNode, methodTransformers);
            return classNode;
        }
    }
}

function createClassMethodsTransformer(className, classTransformer, methodTransformers) {
    return {
        'target': {'type': 'CLASS', 'name': className.replace('.', '/')},
        'transformer': function (classNode) {
            transformClass(className, classNode, classTransformer);
            transformMethods(className, classNode, methodTransformers);
            return classNode;
        }
    }
}

function transformClass(className, classNode, classTransformer) {
    log("Transforming class '" + className + "'...");
    if (!classTransformer(classNode)) {
        log("Couldn't transform class!");
    }
}

function transformMethods(className, classNode, methodTransformers) {
    classNode.methods.forEach(function (methodNode) {
        for (var key in methodTransformers) {
            if (checkNameDesc(methodNode, key)) {
                var srgName = key.substring(0, key.indexOf(' '));
                log("Transforming method '" + className + '.' + methodNode.name + "' (SRG: '" + srgName + "')...");
                if (!methodTransformers[key](methodNode)) {
                    log("Couldn't transform method!");
                }
            }
        }
    });
}

// build method call
function callMethod(type, method) {
    var dotIndex = method.indexOf('.');
    var spaceIndex = method.indexOf(' ');

    var methodOwner = method.substring(0, dotIndex);
    var methodName = method.substring(dotIndex + 1, spaceIndex);
    var methodDesc = method.substring(spaceIndex + 1);

    return ASMAPI.buildMethodCall(methodOwner, methodName, methodDesc, type);
}

// check method opcodes chain
function checkOpcodesChain(instructions, start, chain) {
    if (start < 0 || instructions.size() <= start + chain.length)
        return false;

    for (var i = 0; i < chain.length; i++) {
        var insn = instructions.get(start + i);

        var actual = insn.getOpcode();
        var expected = chain[i];

        if (actual !== expected) {
            log("Mismatched opcodes chain: " + findOpcodeName(actual) + " != " + findOpcodeName(expected) + " (actual/expected)");
            return false;
        }
    }

    return true;
}

// check var instruction
function checkVarInsn(insnNode, opcode, index) {
    if (insnNode.getOpcode() !== opcode)
        return false;

    return insnNode.var === index;
}

// check method instruction
function checkMethodInsn(insnNode, opcode, method) {
    if (insnNode.getOpcode() !== opcode)
        return false;

    return checkNameDesc(insnNode, method);
}

// check field instruction
function checkFieldInsn(insnNode, opcode, field) {
    if (insnNode.getOpcode() !== opcode)
        return false;

    return checkNameDesc(insnNode, field);
}

// check method/field name and desc
function checkNameDesc(insnNode, nameDesc) {
    var spaceIndex = nameDesc.indexOf(' ');
    var name = nameDesc.substring(0, spaceIndex);
    var desc = nameDesc.substring(spaceIndex + 1);
    return insnNode.name.equals(mapName(name)) && insnNode.desc.equals(desc);
}

// get instruction for jumping to label
function getSkipInsn(label) {
    var labelInsn = label.getLabel();
    labelInsn.info = label;
    return labelInsn;
}

// map method/field name
function mapName(srgName) {
    try {
        if (srgName.startsWith("field_")) return ASMAPI.mapField(srgName);
        if (srgName.startsWith("func_")) return ASMAPI.mapMethod(srgName);
    } catch (ignored) {}

    return srgName;
}

// validate entry array
function validateEntryPoints(entry) {
    for (var i = 0; i < entry.length; i++) {
        if (entry[i] === null) {
            log("Entry point #" + i + " not found!");
            return false;
        }
    }

    return true;
}

// get Java class by name
function getJavaType(name) {
    try {
        return Java.type(name);
    } catch (ignored) {
        return null;
    }
}

// print instructions in a human-readable format
function printInstructions(instructions) {
    for (var index = 0, instrcount = instructions.size(); index < instrcount; index++) {
        var instruction = instructions.get(index);

        var indexStr = index + ": ";
        var typeName = findTypeName(instruction.getType());
        var opcodeName = findOpcodeName(instruction.getOpcode());

        // noinspection JSValidateTypes
        while (indexStr.length() < 6) {
            indexStr = " " + indexStr;
        }

        // noinspection JSValidateTypes
        while (typeName.length() < 12) {
            typeName = typeName + " ";
        }

        // noinspection JSValidateTypes
        if (opcodeName.length() > 0) {
            opcodeName = " | " + opcodeName;

            try {
                var name = instruction.name;

                if (name) {
                    opcodeName += ", " + name;
                }
            } catch (ignored) {}

            try {
                var desc = instruction.desc;

                if (desc){
                    opcodeName += ", " + desc;
                }
            } catch (ignored) {}

            try {
                var label = instruction.label;

                if (label) {
                    for (var search = 0; search < instrcount; search++) {
                        if (instructions.get(search) === label) {
                            opcodeName += ", " + search;
                            break;
                        }
                    }
                }
            } catch (ignored) {}
        }

        // noinspection JSCheckFunctionSignatures
        log(indexStr + typeName + opcodeName);
    }
}

// simple logging function
function log(message) {
    if (SHOW_DEBUG_MESSAGES) {
        // noinspection JSCheckFunctionSignatures
        print(message);
    }
}

// find human-readable name for type constant
function findTypeName(type) {
    switch(type){
        case 0: return "Insn";
        case 1: return "IntInsn";
        case 2: return "VarInsn";
        case 3: return "TypeInsn";
        case 4: return "FieldInsn";
        case 5: return "MethodInsn";
        case 6: return "InvokeDynamicInsn";
        case 7: return "JumpInsn";
        case 8: return "Label";
        case 9: return "LdcInsn";
        case 10: return "IincInsn";
        case 11: return "TableSwitchInsn";
        case 12: return "LookupSwitchInsn";
        case 13: return "MultiANewArrayInsn";
        case 14: return "Frame";
        case 15: return "LineNumber";
        default: return "[unknown - " + type + "]";
    }
}

// find human-readable name for opcode constant
function findOpcodeName(opcode) {
    switch(opcode){
        case -1: return "";
        case 0: return "NOP";
        case 1: return "ACONST_NULL";
        case 2: return "ICONST_M1";
        case 3: return "ICONST_0";
        case 4: return "ICONST_1";
        case 5: return "ICONST_2";
        case 6: return "ICONST_3";
        case 7: return "ICONST_4";
        case 8: return "ICONST_5";
        case 9: return "LCONST_0";
        case 10: return "LCONST_1";
        case 11: return "FCONST_0";
        case 12: return "FCONST_1";
        case 13: return "FCONST_2";
        case 14: return "DCONST_0";
        case 15: return "DCONST_1";
        case 16: return "BIPUSH";
        case 17: return "SIPUSH";
        case 18: return "LDC";
        case 21: return "ILOAD";
        case 22: return "LLOAD";
        case 23: return "FLOAD";
        case 24: return "DLOAD";
        case 25: return "ALOAD";
        case 46: return "IALOAD";
        case 47: return "LALOAD";
        case 48: return "FALOAD";
        case 49: return "DALOAD";
        case 50: return "AALOAD";
        case 51: return "BALOAD";
        case 52: return "CALOAD";
        case 53: return "SALOAD";
        case 54: return "ISTORE";
        case 55: return "LSTORE";
        case 56: return "FSTORE";
        case 57: return "DSTORE";
        case 58: return "ASTORE";
        case 79: return "IASTORE";
        case 80: return "LASTORE";
        case 81: return "FASTORE";
        case 82: return "DASTORE";
        case 83: return "AASTORE";
        case 84: return "BASTORE";
        case 85: return "CASTORE";
        case 86: return "SASTORE";
        case 87: return "POP";
        case 88: return "POP2";
        case 89: return "DUP";
        case 90: return "DUP_X1";
        case 91: return "DUP_X2";
        case 92: return "DUP2";
        case 93: return "DUP2_X1";
        case 94: return "DUP2_X2";
        case 95: return "SWAP";
        case 96: return "IADD";
        case 97: return "LADD";
        case 98: return "FADD";
        case 99: return "DADD";
        case 100: return "ISUB";
        case 101: return "LSUB";
        case 102: return "FSUB";
        case 103: return "DSUB";
        case 104: return "IMUL";
        case 105: return "LMUL";
        case 106: return "FMUL";
        case 107: return "DMUL";
        case 108: return "IDIV";
        case 109: return "LDIV";
        case 110: return "FDIV";
        case 111: return "DDIV";
        case 112: return "IREM";
        case 113: return "LREM";
        case 114: return "FREM";
        case 115: return "DREM";
        case 116: return "INEG";
        case 117: return "LNEG";
        case 118: return "FNEG";
        case 119: return "DNEG";
        case 120: return "ISHL";
        case 121: return "LSHL";
        case 122: return "ISHR";
        case 123: return "LSHR";
        case 124: return "IUSHR";
        case 125: return "LUSHR";
        case 126: return "IAND";
        case 127: return "LAND";
        case 128: return "IOR";
        case 129: return "LOR";
        case 130: return "IXOR";
        case 131: return "LXOR";
        case 132: return "IINC";
        case 133: return "I2L";
        case 134: return "I2F";
        case 135: return "I2D";
        case 136: return "L2I";
        case 137: return "L2F";
        case 138: return "L2D";
        case 139: return "F2I";
        case 140: return "F2L";
        case 141: return "F2D";
        case 142: return "D2I";
        case 143: return "D2L";
        case 144: return "D2F";
        case 145: return "I2B";
        case 146: return "I2C";
        case 147: return "I2S";
        case 148: return "LCMP";
        case 149: return "FCMPL";
        case 150: return "FCMPG";
        case 151: return "DCMPL";
        case 152: return "DCMPG";
        case 153: return "IFEQ";
        case 154: return "IFNE";
        case 155: return "IFLT";
        case 156: return "IFGE";
        case 157: return "IFGT";
        case 158: return "IFLE";
        case 159: return "IF_ICMPEQ";
        case 160: return "IF_ICMPNE";
        case 161: return "IF_ICMPLT";
        case 162: return "IF_ICMPGE";
        case 163: return "IF_ICMPGT";
        case 164: return "IF_ICMPLE";
        case 165: return "IF_ACMPEQ";
        case 166: return "IF_ACMPNE";
        case 167: return "GOTO";
        case 168: return "JSR";
        case 169: return "RET";
        case 170: return "TABLESWITCH";
        case 171: return "LOOKUPSWITCH";
        case 172: return "IRETURN";
        case 173: return "LRETURN";
        case 174: return "FRETURN";
        case 175: return "DRETURN";
        case 176: return "ARETURN";
        case 177: return "RETURN";
        case 178: return "GETSTATIC";
        case 179: return "PUTSTATIC";
        case 180: return "GETFIELD";
        case 181: return "PUTFIELD";
        case 182: return "INVOKEVIRTUAL";
        case 183: return "INVOKESPECIAL";
        case 184: return "INVOKESTATIC";
        case 185: return "INVOKEINTERFACE";
        case 186: return "INVOKEDYNAMIC";
        case 187: return "NEW";
        case 188: return "NEWARRAY";
        case 189: return "ANEWARRAY";
        case 190: return "ARRAYLENGTH";
        case 191: return "ATHROW";
        case 192: return "CHECKCAST";
        case 193: return "INSTANCEOF";
        case 194: return "MONITORENTER";
        case 195: return "MONITOREXIT";
        case 197: return "MULTIANEWARRAY";
        case 198: return "IFNULL";
        case 199: return "IFNONNULL";
        default: return "[unknown - " + opcode + "]";
    }
}