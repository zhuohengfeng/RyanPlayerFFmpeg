package com.ryan.playerffmpeg.player;

public interface OnInitializedCallback {

	enum OnInitialStatus{
		CONNECT_SUCESS,
		CONNECT_FAILED,
		CLINET_CANCEL
	};
	public void onInitialized(OnInitialStatus onInitialStatus);
}
