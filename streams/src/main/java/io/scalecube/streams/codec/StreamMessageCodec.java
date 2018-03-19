package io.scalecube.streams.codec;

import static io.scalecube.streams.StreamMessage.DATA_NAME;
import static io.scalecube.streams.StreamMessage.QUALIFIER_NAME;
import static io.scalecube.streams.StreamMessage.SENDER_ID_NAME;
import static io.scalecube.streams.StreamMessage.STREAM_ID_NAME;

import io.scalecube.streams.StreamMessage;

import com.google.common.collect.ImmutableList;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class StreamMessageCodec {

  private static final List<String> FLAT_FIELDS = ImmutableList.of(QUALIFIER_NAME, SENDER_ID_NAME, STREAM_ID_NAME);
  private static final List<String> MATCH_FIELDS = ImmutableList.of(DATA_NAME);

  private StreamMessageCodec() {
    // Do not instantiate
  }

  /**
   * Decodes message from ByteBuf.
   * 
   * @param sourceBuf source buffer; reader index of this buffer will stay unchanged.
   * @return message with sliced data buffer extracted from source buffer.
   * @see ByteBufCodec#decode(ByteBuf, List, List, BiConsumer)
   */
  public static StreamMessage decode(ByteBuf sourceBuf) {
    StreamMessage.Builder messageBuilder = StreamMessage.builder();
    try {
      ByteBufCodec.decode(sourceBuf.slice(), FLAT_FIELDS, MATCH_FIELDS, (fieldName, value) -> {
        switch (fieldName) {
          case QUALIFIER_NAME:
            messageBuilder.qualifier((String) value);
            break;
          case SENDER_ID_NAME:
            messageBuilder.senderId((String) value);
            break;
          case STREAM_ID_NAME:
            messageBuilder.streamId((String) value);
            break;
          case DATA_NAME:
            messageBuilder.data(value); // ByteBuf
            break;
          default:
            // no-op
        }
      });
    } catch (Exception e) {
      throw new DecoderException(e);
    }
    return messageBuilder.build();
  }

  /**
   * Encodes message to ByteBuf.
   * 
   * @param message to encode
   * @return encoded message
   * @see ByteBufCodec#encode(ByteBuf, List, List, Function)
   */
  public static ByteBuf encode(StreamMessage message) {
    ByteBuf targetBuf = ByteBufAllocator.DEFAULT.buffer();
    try {
      ByteBufCodec.encode(targetBuf, FLAT_FIELDS, MATCH_FIELDS, fieldName -> {
        switch (fieldName) {
          case QUALIFIER_NAME:
            return message.getQualifier();
          case SENDER_ID_NAME:
            return message.getSenderId();
          case STREAM_ID_NAME:
            return message.getStreamId();
          case DATA_NAME:
            return message.getData(); // ByteBuf
          default:
            return null;
        }
      });
    } catch (Exception e) {
      targetBuf.release(); // buf belongs to this function => he released in this function
      throw new EncoderException(e);
    }
    return targetBuf;
  }
}
