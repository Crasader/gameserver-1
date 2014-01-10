package com.xinqihd.sns.gameserver.config;

import java.io.Serializable;

/**
 * The common Pojo object.
 */
public interface Pojo extends Serializable {
	
	public void setId(String id);
	
	public String getId();
	
}