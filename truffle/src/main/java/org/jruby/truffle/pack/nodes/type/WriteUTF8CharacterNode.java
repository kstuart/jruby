package org.jruby.truffle.pack.nodes.type;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.jruby.truffle.pack.nodes.PackNode;
import org.jruby.truffle.pack.runtime.RangeException;

import java.nio.charset.StandardCharsets;

@NodeChildren({
        @NodeChild(value = "value", type = PackNode.class),
})
public abstract class WriteUTF8CharacterNode extends PackNode {

    // UTF-8 logic copied from jruby.util.Pack - see copyright and authorship there

    @Specialization(guards = {"value >= 0", "value <= 0x7f"})
    public Object writeSingleByte(VirtualFrame frame, long value) {
        writeBytes(frame,
                (byte) value);
        return null;
    }

    @Specialization(guards = {"value > 0x7f", "value <= 0x7ff"})
    public Object writeTwoBytes(VirtualFrame frame, long value) {
        writeBytes(frame,
                (byte)(((value >>> 6) & 0xff) | 0xc0),
                (byte)((value & 0x3f) | 0x80));
        return null;
    }

    @Specialization(guards = {"value > 0x7ff", "value <= 0xffff"})
    public Object writeThreeBytes(VirtualFrame frame, long value) {
        writeBytes(frame,
                (byte)(((value >>> 12) & 0xff) | 0xe0),
                (byte)(((value >>> 6) & 0x3f) | 0x80),
                (byte)((value & 0x3f) | 0x80));
        return null;
    }

    @Specialization(guards = {"value > 0xffff", "value <= 0x1fffff"})
    public Object writeFourBytes(VirtualFrame frame, long value) {
        writeBytes(frame,
                (byte)(((value >>> 18) & 0xff) | 0xf0),
                (byte)(((value >>> 12) & 0x3f) | 0x80),
                (byte)(((value >>> 6) & 0x3f) | 0x80),
                (byte)((value & 0x3f) | 0x80));
        return null;
    }

    @Specialization(guards = {"value > 0x1fffff", "value <= 0x3ffffff"})
    public Object writeFiveBytes(VirtualFrame frame, long value) {
        writeBytes(frame,
                (byte)(((value >>> 24) & 0xff) | 0xf8),
                (byte)(((value >>> 18) & 0x3f) | 0x80),
                (byte)(((value >>> 12) & 0x3f) | 0x80),
                (byte)(((value >>> 6) & 0x3f) | 0x80),
                (byte)((value & 0x3f) | 0x80));
        return null;
    }

    @Specialization(guards = {"value > 0x3ffffff", "value <= 0x7fffffff"})
    public Object writeSixBytes(VirtualFrame frame, long value) {
        writeBytes(frame,
                (byte)(((value >>> 30) & 0xff) | 0xfc),
                (byte)(((value >>> 24) & 0x3f) | 0x80),
                (byte)(((value >>> 18) & 0x3f) | 0x80),
                (byte)(((value >>> 12) & 0x3f) | 0x80),
                (byte)(((value >>> 6) & 0x3f) | 0x80),
                (byte)((value & 0x3f) | 0x80));
        return null;
    }

    @Specialization(guards = "value < 0")
    public Object writeNegative(VirtualFrame frame, long value) {
        CompilerDirectives.transferToInterpreter();
        throw new RangeException("pack(U): value out of range");
    }

    @Specialization(guards = "value > 0x7fffffff")
    public Object writeOutOfRange(VirtualFrame frame, long value) {
        CompilerDirectives.transferToInterpreter();
        throw new RangeException("pack(U): value out of range");
    }

}
