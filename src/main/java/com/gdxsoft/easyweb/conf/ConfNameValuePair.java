package com.gdxsoft.easyweb.conf;

public class ConfNameValuePair {
	private String name;
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return name;
		}
		final int len = this.name.length() + 1 + this.value.length();
		final StringBuilder buffer = new StringBuilder(len);
		buffer.append(this.name);
		buffer.append("=");
		buffer.append(this.value);
		return buffer.toString();
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) {
			return false;
		}
		if (this == object) {
			return true;
		}
		if (object instanceof ConfNameValuePair) {
			final ConfNameValuePair that = (ConfNameValuePair) object;
			return this.name != null && this.name.equals(that.name) && this.value != null
					&& this.value.equals(that.value);
		}
		return false;
	}

}
