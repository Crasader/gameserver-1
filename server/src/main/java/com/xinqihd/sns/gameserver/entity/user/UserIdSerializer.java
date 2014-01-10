package com.xinqihd.sns.gameserver.entity.user;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class UserIdSerializer extends Serializer<UserId> {

	@Override
	public void write(Kryo kryo, Output output, UserId userId) {
		byte[] bytes = userId.getInternal();
		output.writeShort(bytes.length);
		output.write(bytes);
	}

	@Override
	public UserId read(Kryo kryo, Input input, Class type) {
		int length = input.readShort();
		byte[] array = new byte[length];
		input.readBytes(array);
		return UserId.fromBytes(array);
	}

}
