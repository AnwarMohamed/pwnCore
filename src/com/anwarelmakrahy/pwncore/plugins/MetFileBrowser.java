package com.anwarelmakrahy.pwncore.plugins;

import java.util.Map;

import org.msgpack.type.Value;

import android.content.Context;

import com.anwarelmakrahy.pwncore.console.ConsoleSession.ConsoleSessionParams;
import com.anwarelmakrahy.pwncore.console.ControlSession;

public class MetFileBrowser extends ControlSession {

	MetFileBrowser(Context context, String type, String id,
			Map<String, Value> info, ConsoleSessionParams params) {
		super(context, type, id, info, params);
	}

	MetFileBrowser(Context context, String type, String id,
			Map<String, Value> info) {
		super(context, type, id, info);
	}

}
