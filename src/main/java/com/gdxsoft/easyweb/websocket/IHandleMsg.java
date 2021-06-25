package com.gdxsoft.easyweb.websocket;

public interface IHandleMsg {

	void run();

	String getMethod();

	void setName(String name);

	void start();
}