/*----------------------------------------------------------------------------*/
/* This file is generated by the templates/template.rb script and should not  */
/* be modified manually. See                                                  */
/* templates/java/org/prism/Loader.java.erb                                   */
/* if you are looking to modify the                                           */
/* template                                                                   */
/*----------------------------------------------------------------------------*/

package org.prism;

import org.prism.Nodes;

import java.lang.Short;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

// GENERATED BY Loader.java.erb
// @formatter:off
public class Loader {

    public static ParseResult load(byte[] serialized, byte[] sourceBytes) {
        return new Loader(serialized, sourceBytes).load();
    }

    // Overridable methods

    public Charset getEncodingCharset(String encodingName) {
        encodingName = encodingName.toLowerCase(Locale.ROOT);
        if (encodingName.equals("ascii-8bit")) {
            return StandardCharsets.US_ASCII;
        }
        return Charset.forName(encodingName);
    }

    public String bytesToName(byte[] bytes) {
        return new String(bytes, encodingCharset).intern();
    }

    private static final class ConstantPool {

        private final Loader loader;
        private final byte[] source;
        private final int bufferOffset;
        private final String[] cache;

        ConstantPool(Loader loader, byte[] source, int bufferOffset, int length) {
            this.loader = loader;
            this.source = source;
            this.bufferOffset = bufferOffset;
            cache = new String[length];
        }

        String get(ByteBuffer buffer, int oneBasedIndex) {
            int index = oneBasedIndex - 1;
            String constant = cache[index];

            if (constant == null) {
                int offset = bufferOffset + index * 8;
                int start = buffer.getInt(offset);
                int length = buffer.getInt(offset + 4);

                byte[] bytes = new byte[length];

                if (Integer.compareUnsigned(start, 0x7FFFFFFF) <= 0) {
                    System.arraycopy(source, start, bytes, 0, length);
                } else {
                    int position = buffer.position();
                    buffer.position(start & 0x7FFFFFFF);
                    buffer.get(bytes, 0, length);
                    buffer.position(position);
                }

                constant = loader.bytesToName(bytes);
                cache[index] = constant;
            }

            return constant;
        }

    }

    private final ByteBuffer buffer;
    private final Nodes.Source source;
    protected String encodingName;
    private Charset encodingCharset;
    private ConstantPool constantPool;

    protected Loader(byte[] serialized, byte[] sourceBytes) {
        this.buffer = ByteBuffer.wrap(serialized).order(ByteOrder.nativeOrder());
        this.source = new Nodes.Source(sourceBytes);
    }

    protected ParseResult load() {
        expect((byte) 'P', "incorrect prism header");
        expect((byte) 'R', "incorrect prism header");
        expect((byte) 'I', "incorrect prism header");
        expect((byte) 'S', "incorrect prism header");
        expect((byte) 'M', "incorrect prism header");

        expect((byte) 1, "prism major version does not match");
        expect((byte) 1, "prism minor version does not match");
        expect((byte) 0, "prism patch version does not match");

        expect((byte) 1, "Loader.java requires no location fields in the serialized output");

        // This loads the name of the encoding.
        int encodingLength = loadVarUInt();
        byte[] encodingNameBytes = new byte[encodingLength];
        buffer.get(encodingNameBytes);
        this.encodingName = new String(encodingNameBytes, StandardCharsets.US_ASCII);
        this.encodingCharset = getEncodingCharset(this.encodingName);

        source.setStartLine(loadVarSInt());
        source.setLineOffsets(loadLineOffsets());

        ParseResult.MagicComment[] magicComments = loadMagicComments();
        Nodes.Location dataLocation = loadOptionalLocation();
        ParseResult.Error[] errors = loadErrors();
        ParseResult.Warning[] warnings = loadWarnings();

        int constantPoolBufferOffset = buffer.getInt();
        int constantPoolLength = loadVarUInt();
        this.constantPool = new ConstantPool(this, source.bytes, constantPoolBufferOffset, constantPoolLength);

        Nodes.Node node;
        if (errors.length == 0) {
            node = loadNode();

            int left = constantPoolBufferOffset - buffer.position();
            if (left != 0) {
                throw new Error("Expected to consume all bytes while deserializing but there were " + left + " bytes left");
            }

            boolean[] newlineMarked = new boolean[1 + source.getLineCount()];
            MarkNewlinesVisitor visitor = new MarkNewlinesVisitor(source, newlineMarked);
            node.accept(visitor);
        } else {
            node = null;
        }

        return new ParseResult(node, magicComments, dataLocation, errors, warnings, source);
    }

    private byte[] loadEmbeddedString() {
        int length = loadVarUInt();
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    private byte[] loadString() {
        switch (buffer.get()) {
            case 1:
                int start = loadVarUInt();
                int length = loadVarUInt();
                byte[] bytes = new byte[length];
                System.arraycopy(source.bytes, start, bytes, 0, length);
                return bytes;
            case 2:
                return loadEmbeddedString();
            default:
                throw new Error("Expected 0 or 1 but was " + buffer.get());
        }
    }

    private int[] loadLineOffsets() {
        int count = loadVarUInt();
        int[] lineOffsets = new int[count];
        for (int i = 0; i < count; i++) {
            lineOffsets[i] = loadVarUInt();
        }
        return lineOffsets;
    }

    private ParseResult.MagicComment[] loadMagicComments() {
        int count = loadVarUInt();
        ParseResult.MagicComment[] magicComments = new ParseResult.MagicComment[count];

        for (int i = 0; i < count; i++) {
            Nodes.Location keyLocation = loadLocation();
            Nodes.Location valueLocation = loadLocation();

            ParseResult.MagicComment magicComment = new ParseResult.MagicComment(keyLocation, valueLocation);
            magicComments[i] = magicComment;
        }

        return magicComments;
    }

    private ParseResult.Error[] loadErrors() {
        int count = loadVarUInt();
        ParseResult.Error[] errors = new ParseResult.Error[count];

        // error messages only contain ASCII characters
        for (int i = 0; i < count; i++) {
            Nodes.ErrorType type = Nodes.ERROR_TYPES[loadVarUInt()];
            byte[] bytes = loadEmbeddedString();
            String message = new String(bytes, StandardCharsets.US_ASCII);
            Nodes.Location location = loadLocation();
            ParseResult.ErrorLevel level = ParseResult.ERROR_LEVELS[buffer.get()];

            ParseResult.Error error = new ParseResult.Error(type, message, location, level);
            errors[i] = error;
        }

        return errors;
    }

    private ParseResult.Warning[] loadWarnings() {
        int count = loadVarUInt();
        ParseResult.Warning[] warnings = new ParseResult.Warning[count];

        // warning messages only contain ASCII characters
        for (int i = 0; i < count; i++) {
            Nodes.WarningType type = Nodes.WARNING_TYPES[loadVarUInt() - 288];
            byte[] bytes = loadEmbeddedString();
            String message = new String(bytes, StandardCharsets.US_ASCII);
            Nodes.Location location = loadLocation();
            ParseResult.WarningLevel level = ParseResult.WARNING_LEVELS[buffer.get()];

            ParseResult.Warning warning = new ParseResult.Warning(type, message, location, level);
            warnings[i] = warning;
        }

        return warnings;
    }

    private Nodes.Node loadOptionalNode() {
        if (buffer.get(buffer.position()) != 0) {
            return loadNode();
        } else {
            buffer.position(buffer.position() + 1); // continue after the 0 byte
            return null;
        }
    }

    private String loadConstant() {
        return constantPool.get(buffer, loadVarUInt());
    }

    private String loadOptionalConstant() {
        if (buffer.get(buffer.position()) != 0) {
            return loadConstant();
        } else {
            buffer.position(buffer.position() + 1); // continue after the 0 byte
            return null;
        }
    }

    private String[] loadConstants() {
        int length = loadVarUInt();
        if (length == 0) {
            return Nodes.EMPTY_STRING_ARRAY;
        }
        String[] constants = new String[length];
        for (int i = 0; i < length; i++) {
            constants[i] = constantPool.get(buffer, loadVarUInt());
        }
        return constants;
    }

    private Nodes.Location loadLocation() {
        return new Nodes.Location(loadVarUInt(), loadVarUInt());
    }

    private Nodes.Location loadOptionalLocation() {
        if (buffer.get() != 0) {
            return loadLocation();
        } else {
            return null;
        }
    }

    // From https://github.com/protocolbuffers/protobuf/blob/v23.1/java/core/src/main/java/com/google/protobuf/BinaryReader.java#L1507
    private int loadVarUInt() {
        int x;
        if ((x = buffer.get()) >= 0) {
            return x;
        } else if ((x ^= (buffer.get() << 7)) < 0) {
            x ^= (~0 << 7);
        } else if ((x ^= (buffer.get() << 14)) >= 0) {
            x ^= (~0 << 7) ^ (~0 << 14);
        } else if ((x ^= (buffer.get() << 21)) < 0) {
            x ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21);
        } else {
            x ^= buffer.get() << 28;
            x ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21) ^ (~0 << 28);
        }
        return x;
    }

    // From https://github.com/protocolbuffers/protobuf/blob/v25.1/java/core/src/main/java/com/google/protobuf/CodedInputStream.java#L508-L510
    private int loadVarSInt() {
        int x = loadVarUInt();
        return (x >>> 1) ^ (-(x & 1));
    }

    private short loadFlags() {
        int flags = loadVarUInt();
        assert flags >= 0 && flags <= Short.MAX_VALUE;
        return (short) flags;
    }

    private static final BigInteger UNSIGNED_LONG_MASK = BigInteger.ONE.shiftLeft(Long.SIZE).subtract(BigInteger.ONE);

    private Object loadInteger() {
        boolean negative = buffer.get() != 0;

        int wordsLength = loadVarUInt();
        assert wordsLength > 0;

        // Load the first word. If it's the only word, then return an int if it
        // fits into one and a long otherwise.
        int firstWord = loadVarUInt();
        if (wordsLength == 1) {
            if (firstWord < 0) {
                if (negative && firstWord == Integer.MIN_VALUE) {
                    return Integer.MIN_VALUE;
                }

                long words = Integer.toUnsignedLong(firstWord);
                return negative ? -words : words;
            }
            return negative ? -firstWord : firstWord;
        }

        // Load the second word. If there are only two words, then return a long
        // if it fits into one and a BigInteger otherwise.
        int secondWord = loadVarUInt();
        if (wordsLength == 2) {
            long words = (((long) secondWord) << 32L) | Integer.toUnsignedLong(firstWord);
            if (words < 0L) {
                if (negative && words == Long.MIN_VALUE) {
                    return Long.MIN_VALUE;
                }

                BigInteger result = BigInteger.valueOf(words).and(UNSIGNED_LONG_MASK);
                return negative ? result.negate() : result;
            }
            return negative ? -words : words;
        }

        // Otherwise, load the remaining words and return a BigInt.
        BigInteger result = BigInteger.valueOf(Integer.toUnsignedLong(firstWord));
        result = result.or(BigInteger.valueOf(Integer.toUnsignedLong(secondWord)).shiftLeft(32));

        for (int wordsIndex = 2; wordsIndex < wordsLength; wordsIndex++) {
            result = result.or(BigInteger.valueOf(Integer.toUnsignedLong(loadVarUInt())).shiftLeft(wordsIndex * 32));
        }

        return negative ? result.negate() : result;
    }

    private Nodes.Node loadNode() {
        int type = buffer.get() & 0xFF;
        int startOffset = loadVarUInt();
        int length = loadVarUInt();

        switch (type) {
            case 1:
                return new Nodes.AliasGlobalVariableNode(startOffset, length, loadNode(), loadNode());
            case 2:
                return new Nodes.AliasMethodNode(startOffset, length, loadNode(), loadNode());
            case 3:
                return new Nodes.AlternationPatternNode(startOffset, length, loadNode(), loadNode());
            case 4:
                return new Nodes.AndNode(startOffset, length, loadNode(), loadNode());
            case 5:
                return new Nodes.ArgumentsNode(startOffset, length, loadFlags(), loadNodes());
            case 6:
                return new Nodes.ArrayNode(startOffset, length, loadFlags(), loadNodes());
            case 7:
                return new Nodes.ArrayPatternNode(startOffset, length, loadOptionalNode(), loadNodes(), loadOptionalNode(), loadNodes());
            case 8:
                return new Nodes.AssocNode(startOffset, length, loadNode(), loadNode());
            case 9:
                return new Nodes.AssocSplatNode(startOffset, length, loadOptionalNode());
            case 10:
                return new Nodes.BackReferenceReadNode(startOffset, length, loadConstant());
            case 11:
                return new Nodes.BeginNode(startOffset, length, (Nodes.StatementsNode) loadOptionalNode(), (Nodes.RescueNode) loadOptionalNode(), (Nodes.ElseNode) loadOptionalNode(), (Nodes.EnsureNode) loadOptionalNode());
            case 12:
                return new Nodes.BlockArgumentNode(startOffset, length, loadOptionalNode());
            case 13:
                return new Nodes.BlockLocalVariableNode(startOffset, length, loadFlags(), loadConstant());
            case 14:
                return new Nodes.BlockNode(startOffset, length, loadConstants(), loadOptionalNode(), loadOptionalNode());
            case 15:
                return new Nodes.BlockParameterNode(startOffset, length, loadFlags(), loadOptionalConstant());
            case 16:
                return new Nodes.BlockParametersNode(startOffset, length, (Nodes.ParametersNode) loadOptionalNode(), loadBlockLocalVariableNodes());
            case 17:
                return new Nodes.BreakNode(startOffset, length, (Nodes.ArgumentsNode) loadOptionalNode());
            case 18:
                return new Nodes.CallAndWriteNode(startOffset, length, loadFlags(), loadOptionalNode(), loadConstant(), loadConstant(), loadNode());
            case 19:
                return new Nodes.CallNode(startOffset, length, loadFlags(), loadOptionalNode(), loadConstant(), (Nodes.ArgumentsNode) loadOptionalNode(), loadOptionalNode());
            case 20:
                return new Nodes.CallOperatorWriteNode(startOffset, length, loadFlags(), loadOptionalNode(), loadConstant(), loadConstant(), loadConstant(), loadNode());
            case 21:
                return new Nodes.CallOrWriteNode(startOffset, length, loadFlags(), loadOptionalNode(), loadConstant(), loadConstant(), loadNode());
            case 22:
                return new Nodes.CallTargetNode(startOffset, length, loadFlags(), loadNode(), loadConstant());
            case 23:
                return new Nodes.CapturePatternNode(startOffset, length, loadNode(), (Nodes.LocalVariableTargetNode) loadNode());
            case 24:
                return new Nodes.CaseMatchNode(startOffset, length, loadOptionalNode(), loadInNodes(), (Nodes.ElseNode) loadOptionalNode());
            case 25:
                return new Nodes.CaseNode(startOffset, length, loadOptionalNode(), loadWhenNodes(), (Nodes.ElseNode) loadOptionalNode());
            case 26:
                return new Nodes.ClassNode(startOffset, length, loadConstants(), loadNode(), loadOptionalNode(), loadOptionalNode(), loadConstant());
            case 27:
                return new Nodes.ClassVariableAndWriteNode(startOffset, length, loadConstant(), loadNode());
            case 28:
                return new Nodes.ClassVariableOperatorWriteNode(startOffset, length, loadConstant(), loadNode(), loadConstant());
            case 29:
                return new Nodes.ClassVariableOrWriteNode(startOffset, length, loadConstant(), loadNode());
            case 30:
                return new Nodes.ClassVariableReadNode(startOffset, length, loadConstant());
            case 31:
                return new Nodes.ClassVariableTargetNode(startOffset, length, loadConstant());
            case 32:
                return new Nodes.ClassVariableWriteNode(startOffset, length, loadConstant(), loadNode());
            case 33:
                return new Nodes.ConstantAndWriteNode(startOffset, length, loadConstant(), loadNode());
            case 34:
                return new Nodes.ConstantOperatorWriteNode(startOffset, length, loadConstant(), loadNode(), loadConstant());
            case 35:
                return new Nodes.ConstantOrWriteNode(startOffset, length, loadConstant(), loadNode());
            case 36:
                return new Nodes.ConstantPathAndWriteNode(startOffset, length, (Nodes.ConstantPathNode) loadNode(), loadNode());
            case 37:
                return new Nodes.ConstantPathNode(startOffset, length, loadOptionalNode(), loadOptionalConstant());
            case 38:
                return new Nodes.ConstantPathOperatorWriteNode(startOffset, length, (Nodes.ConstantPathNode) loadNode(), loadNode(), loadConstant());
            case 39:
                return new Nodes.ConstantPathOrWriteNode(startOffset, length, (Nodes.ConstantPathNode) loadNode(), loadNode());
            case 40:
                return new Nodes.ConstantPathTargetNode(startOffset, length, loadOptionalNode(), loadOptionalConstant());
            case 41:
                return new Nodes.ConstantPathWriteNode(startOffset, length, (Nodes.ConstantPathNode) loadNode(), loadNode());
            case 42:
                return new Nodes.ConstantReadNode(startOffset, length, loadConstant());
            case 43:
                return new Nodes.ConstantTargetNode(startOffset, length, loadConstant());
            case 44:
                return new Nodes.ConstantWriteNode(startOffset, length, loadConstant(), loadNode());
            case 45:
                return new Nodes.DefNode(startOffset, length, buffer.getInt(), loadConstant(), loadOptionalNode(), (Nodes.ParametersNode) loadOptionalNode(), loadOptionalNode(), loadConstants());
            case 46:
                return new Nodes.DefinedNode(startOffset, length, loadNode());
            case 47:
                return new Nodes.ElseNode(startOffset, length, (Nodes.StatementsNode) loadOptionalNode());
            case 48:
                return new Nodes.EmbeddedStatementsNode(startOffset, length, (Nodes.StatementsNode) loadOptionalNode());
            case 49:
                return new Nodes.EmbeddedVariableNode(startOffset, length, loadNode());
            case 50:
                return new Nodes.EnsureNode(startOffset, length, (Nodes.StatementsNode) loadOptionalNode());
            case 51:
                return new Nodes.FalseNode(startOffset, length);
            case 52:
                return new Nodes.FindPatternNode(startOffset, length, loadOptionalNode(), (Nodes.SplatNode) loadNode(), loadNodes(), (Nodes.SplatNode) loadNode());
            case 53:
                return new Nodes.FlipFlopNode(startOffset, length, loadFlags(), loadOptionalNode(), loadOptionalNode());
            case 54:
                return new Nodes.FloatNode(startOffset, length, buffer.getDouble());
            case 55:
                return new Nodes.ForNode(startOffset, length, loadNode(), loadNode(), (Nodes.StatementsNode) loadOptionalNode());
            case 56:
                return new Nodes.ForwardingArgumentsNode(startOffset, length);
            case 57:
                return new Nodes.ForwardingParameterNode(startOffset, length);
            case 58:
                return new Nodes.ForwardingSuperNode(startOffset, length, (Nodes.BlockNode) loadOptionalNode());
            case 59:
                return new Nodes.GlobalVariableAndWriteNode(startOffset, length, loadConstant(), loadNode());
            case 60:
                return new Nodes.GlobalVariableOperatorWriteNode(startOffset, length, loadConstant(), loadNode(), loadConstant());
            case 61:
                return new Nodes.GlobalVariableOrWriteNode(startOffset, length, loadConstant(), loadNode());
            case 62:
                return new Nodes.GlobalVariableReadNode(startOffset, length, loadConstant());
            case 63:
                return new Nodes.GlobalVariableTargetNode(startOffset, length, loadConstant());
            case 64:
                return new Nodes.GlobalVariableWriteNode(startOffset, length, loadConstant(), loadNode());
            case 65:
                return new Nodes.HashNode(startOffset, length, loadNodes());
            case 66:
                return new Nodes.HashPatternNode(startOffset, length, loadOptionalNode(), loadAssocNodes(), loadOptionalNode());
            case 67:
                return new Nodes.IfNode(startOffset, length, loadNode(), (Nodes.StatementsNode) loadOptionalNode(), loadOptionalNode());
            case 68:
                return new Nodes.ImaginaryNode(startOffset, length, loadNode());
            case 69:
                return new Nodes.ImplicitNode(startOffset, length, loadNode());
            case 70:
                return new Nodes.ImplicitRestNode(startOffset, length);
            case 71:
                return new Nodes.InNode(startOffset, length, loadNode(), (Nodes.StatementsNode) loadOptionalNode());
            case 72:
                return new Nodes.IndexAndWriteNode(startOffset, length, loadFlags(), loadOptionalNode(), (Nodes.ArgumentsNode) loadOptionalNode(), (Nodes.BlockArgumentNode) loadOptionalNode(), loadNode());
            case 73:
                return new Nodes.IndexOperatorWriteNode(startOffset, length, loadFlags(), loadOptionalNode(), (Nodes.ArgumentsNode) loadOptionalNode(), (Nodes.BlockArgumentNode) loadOptionalNode(), loadConstant(), loadNode());
            case 74:
                return new Nodes.IndexOrWriteNode(startOffset, length, loadFlags(), loadOptionalNode(), (Nodes.ArgumentsNode) loadOptionalNode(), (Nodes.BlockArgumentNode) loadOptionalNode(), loadNode());
            case 75:
                return new Nodes.IndexTargetNode(startOffset, length, loadFlags(), loadNode(), (Nodes.ArgumentsNode) loadOptionalNode(), (Nodes.BlockArgumentNode) loadOptionalNode());
            case 76:
                return new Nodes.InstanceVariableAndWriteNode(startOffset, length, loadConstant(), loadNode());
            case 77:
                return new Nodes.InstanceVariableOperatorWriteNode(startOffset, length, loadConstant(), loadNode(), loadConstant());
            case 78:
                return new Nodes.InstanceVariableOrWriteNode(startOffset, length, loadConstant(), loadNode());
            case 79:
                return new Nodes.InstanceVariableReadNode(startOffset, length, loadConstant());
            case 80:
                return new Nodes.InstanceVariableTargetNode(startOffset, length, loadConstant());
            case 81:
                return new Nodes.InstanceVariableWriteNode(startOffset, length, loadConstant(), loadNode());
            case 82:
                return new Nodes.IntegerNode(startOffset, length, loadFlags(), loadInteger());
            case 83:
                return new Nodes.InterpolatedMatchLastLineNode(startOffset, length, loadFlags(), loadNodes());
            case 84:
                return new Nodes.InterpolatedRegularExpressionNode(startOffset, length, loadFlags(), loadNodes());
            case 85:
                return new Nodes.InterpolatedStringNode(startOffset, length, loadFlags(), loadNodes());
            case 86:
                return new Nodes.InterpolatedSymbolNode(startOffset, length, loadNodes());
            case 87:
                return new Nodes.InterpolatedXStringNode(startOffset, length, loadNodes());
            case 88:
                return new Nodes.ItLocalVariableReadNode(startOffset, length);
            case 89:
                return new Nodes.ItParametersNode(startOffset, length);
            case 90:
                return new Nodes.KeywordHashNode(startOffset, length, loadFlags(), loadNodes());
            case 91:
                return new Nodes.KeywordRestParameterNode(startOffset, length, loadFlags(), loadOptionalConstant());
            case 92:
                return new Nodes.LambdaNode(startOffset, length, loadConstants(), loadOptionalNode(), loadOptionalNode());
            case 93:
                return new Nodes.LocalVariableAndWriteNode(startOffset, length, loadNode(), loadConstant(), loadVarUInt());
            case 94:
                return new Nodes.LocalVariableOperatorWriteNode(startOffset, length, loadNode(), loadConstant(), loadConstant(), loadVarUInt());
            case 95:
                return new Nodes.LocalVariableOrWriteNode(startOffset, length, loadNode(), loadConstant(), loadVarUInt());
            case 96:
                return new Nodes.LocalVariableReadNode(startOffset, length, loadConstant(), loadVarUInt());
            case 97:
                return new Nodes.LocalVariableTargetNode(startOffset, length, loadConstant(), loadVarUInt());
            case 98:
                return new Nodes.LocalVariableWriteNode(startOffset, length, loadConstant(), loadVarUInt(), loadNode());
            case 99:
                return new Nodes.MatchLastLineNode(startOffset, length, loadFlags(), loadString());
            case 100:
                return new Nodes.MatchPredicateNode(startOffset, length, loadNode(), loadNode());
            case 101:
                return new Nodes.MatchRequiredNode(startOffset, length, loadNode(), loadNode());
            case 102:
                return new Nodes.MatchWriteNode(startOffset, length, (Nodes.CallNode) loadNode(), loadLocalVariableTargetNodes());
            case 103:
                return new Nodes.MissingNode(startOffset, length);
            case 104:
                return new Nodes.ModuleNode(startOffset, length, loadConstants(), loadNode(), loadOptionalNode(), loadConstant());
            case 105:
                return new Nodes.MultiTargetNode(startOffset, length, loadNodes(), loadOptionalNode(), loadNodes());
            case 106:
                return new Nodes.MultiWriteNode(startOffset, length, loadNodes(), loadOptionalNode(), loadNodes(), loadNode());
            case 107:
                return new Nodes.NextNode(startOffset, length, (Nodes.ArgumentsNode) loadOptionalNode());
            case 108:
                return new Nodes.NilNode(startOffset, length);
            case 109:
                return new Nodes.NoKeywordsParameterNode(startOffset, length);
            case 110:
                return new Nodes.NumberedParametersNode(startOffset, length, buffer.get());
            case 111:
                return new Nodes.NumberedReferenceReadNode(startOffset, length, loadVarUInt());
            case 112:
                return new Nodes.OptionalKeywordParameterNode(startOffset, length, loadFlags(), loadConstant(), loadNode());
            case 113:
                return new Nodes.OptionalParameterNode(startOffset, length, loadFlags(), loadConstant(), loadNode());
            case 114:
                return new Nodes.OrNode(startOffset, length, loadNode(), loadNode());
            case 115:
                return new Nodes.ParametersNode(startOffset, length, loadNodes(), loadOptionalParameterNodes(), loadOptionalNode(), loadNodes(), loadNodes(), loadOptionalNode(), (Nodes.BlockParameterNode) loadOptionalNode());
            case 116:
                return new Nodes.ParenthesesNode(startOffset, length, loadOptionalNode());
            case 117:
                return new Nodes.PinnedExpressionNode(startOffset, length, loadNode());
            case 118:
                return new Nodes.PinnedVariableNode(startOffset, length, loadNode());
            case 119:
                return new Nodes.PostExecutionNode(startOffset, length, (Nodes.StatementsNode) loadOptionalNode());
            case 120:
                return new Nodes.PreExecutionNode(startOffset, length, (Nodes.StatementsNode) loadOptionalNode());
            case 121:
                return new Nodes.ProgramNode(startOffset, length, loadConstants(), (Nodes.StatementsNode) loadNode());
            case 122:
                return new Nodes.RangeNode(startOffset, length, loadFlags(), loadOptionalNode(), loadOptionalNode());
            case 123:
                return new Nodes.RationalNode(startOffset, length, loadFlags(), loadInteger(), loadInteger());
            case 124:
                return new Nodes.RedoNode(startOffset, length);
            case 125:
                return new Nodes.RegularExpressionNode(startOffset, length, loadFlags(), loadString());
            case 126:
                return new Nodes.RequiredKeywordParameterNode(startOffset, length, loadFlags(), loadConstant());
            case 127:
                return new Nodes.RequiredParameterNode(startOffset, length, loadFlags(), loadConstant());
            case 128:
                return new Nodes.RescueModifierNode(startOffset, length, loadNode(), loadNode());
            case 129:
                return new Nodes.RescueNode(startOffset, length, loadNodes(), loadOptionalNode(), (Nodes.StatementsNode) loadOptionalNode(), (Nodes.RescueNode) loadOptionalNode());
            case 130:
                return new Nodes.RestParameterNode(startOffset, length, loadFlags(), loadOptionalConstant());
            case 131:
                return new Nodes.RetryNode(startOffset, length);
            case 132:
                return new Nodes.ReturnNode(startOffset, length, (Nodes.ArgumentsNode) loadOptionalNode());
            case 133:
                return new Nodes.SelfNode(startOffset, length);
            case 134:
                return new Nodes.ShareableConstantNode(startOffset, length, loadFlags(), loadNode());
            case 135:
                return new Nodes.SingletonClassNode(startOffset, length, loadConstants(), loadNode(), loadOptionalNode());
            case 136:
                return new Nodes.SourceEncodingNode(startOffset, length);
            case 137:
                return new Nodes.SourceFileNode(startOffset, length, loadFlags(), loadString());
            case 138:
                return new Nodes.SourceLineNode(startOffset, length);
            case 139:
                return new Nodes.SplatNode(startOffset, length, loadOptionalNode());
            case 140:
                return new Nodes.StatementsNode(startOffset, length, loadNodes());
            case 141:
                return new Nodes.StringNode(startOffset, length, loadFlags(), loadString());
            case 142:
                return new Nodes.SuperNode(startOffset, length, (Nodes.ArgumentsNode) loadOptionalNode(), loadOptionalNode());
            case 143:
                return new Nodes.SymbolNode(startOffset, length, loadFlags(), loadString());
            case 144:
                return new Nodes.TrueNode(startOffset, length);
            case 145:
                return new Nodes.UndefNode(startOffset, length, loadNodes());
            case 146:
                return new Nodes.UnlessNode(startOffset, length, loadNode(), (Nodes.StatementsNode) loadOptionalNode(), (Nodes.ElseNode) loadOptionalNode());
            case 147:
                return new Nodes.UntilNode(startOffset, length, loadFlags(), loadNode(), (Nodes.StatementsNode) loadOptionalNode());
            case 148:
                return new Nodes.WhenNode(startOffset, length, loadNodes(), (Nodes.StatementsNode) loadOptionalNode());
            case 149:
                return new Nodes.WhileNode(startOffset, length, loadFlags(), loadNode(), (Nodes.StatementsNode) loadOptionalNode());
            case 150:
                return new Nodes.XStringNode(startOffset, length, loadFlags(), loadString());
            case 151:
                return new Nodes.YieldNode(startOffset, length, (Nodes.ArgumentsNode) loadOptionalNode());
            default:
                throw new Error("Unknown node type: " + type);
        }
    }

    private static final Nodes.Node[] EMPTY_Node_ARRAY = {};

    private Nodes.Node[] loadNodes() {
        int length = loadVarUInt();
        if (length == 0) {
            return EMPTY_Node_ARRAY;
        }
        Nodes.Node[] nodes = new Nodes.Node[length];
        for (int i = 0; i < length; i++) {
            nodes[i] = loadNode();
        }
        return nodes;
    }

    private static final Nodes.BlockLocalVariableNode[] EMPTY_BlockLocalVariableNode_ARRAY = {};

    private Nodes.BlockLocalVariableNode[] loadBlockLocalVariableNodes() {
        int length = loadVarUInt();
        if (length == 0) {
            return EMPTY_BlockLocalVariableNode_ARRAY;
        }
        Nodes.BlockLocalVariableNode[] nodes = new Nodes.BlockLocalVariableNode[length];
        for (int i = 0; i < length; i++) {
            nodes[i] = (Nodes.BlockLocalVariableNode) loadNode();
        }
        return nodes;
    }

    private static final Nodes.InNode[] EMPTY_InNode_ARRAY = {};

    private Nodes.InNode[] loadInNodes() {
        int length = loadVarUInt();
        if (length == 0) {
            return EMPTY_InNode_ARRAY;
        }
        Nodes.InNode[] nodes = new Nodes.InNode[length];
        for (int i = 0; i < length; i++) {
            nodes[i] = (Nodes.InNode) loadNode();
        }
        return nodes;
    }

    private static final Nodes.WhenNode[] EMPTY_WhenNode_ARRAY = {};

    private Nodes.WhenNode[] loadWhenNodes() {
        int length = loadVarUInt();
        if (length == 0) {
            return EMPTY_WhenNode_ARRAY;
        }
        Nodes.WhenNode[] nodes = new Nodes.WhenNode[length];
        for (int i = 0; i < length; i++) {
            nodes[i] = (Nodes.WhenNode) loadNode();
        }
        return nodes;
    }

    private static final Nodes.AssocNode[] EMPTY_AssocNode_ARRAY = {};

    private Nodes.AssocNode[] loadAssocNodes() {
        int length = loadVarUInt();
        if (length == 0) {
            return EMPTY_AssocNode_ARRAY;
        }
        Nodes.AssocNode[] nodes = new Nodes.AssocNode[length];
        for (int i = 0; i < length; i++) {
            nodes[i] = (Nodes.AssocNode) loadNode();
        }
        return nodes;
    }

    private static final Nodes.LocalVariableTargetNode[] EMPTY_LocalVariableTargetNode_ARRAY = {};

    private Nodes.LocalVariableTargetNode[] loadLocalVariableTargetNodes() {
        int length = loadVarUInt();
        if (length == 0) {
            return EMPTY_LocalVariableTargetNode_ARRAY;
        }
        Nodes.LocalVariableTargetNode[] nodes = new Nodes.LocalVariableTargetNode[length];
        for (int i = 0; i < length; i++) {
            nodes[i] = (Nodes.LocalVariableTargetNode) loadNode();
        }
        return nodes;
    }

    private static final Nodes.OptionalParameterNode[] EMPTY_OptionalParameterNode_ARRAY = {};

    private Nodes.OptionalParameterNode[] loadOptionalParameterNodes() {
        int length = loadVarUInt();
        if (length == 0) {
            return EMPTY_OptionalParameterNode_ARRAY;
        }
        Nodes.OptionalParameterNode[] nodes = new Nodes.OptionalParameterNode[length];
        for (int i = 0; i < length; i++) {
            nodes[i] = (Nodes.OptionalParameterNode) loadNode();
        }
        return nodes;
    }

    private void expect(byte value, String error) {
        byte b = buffer.get();
        if (b != value) {
            throw new Error("Deserialization error: " + error + " (expected " + value + " but was " + b + " at position " + buffer.position() + ")");
        }
    }

}
// @formatter:on
