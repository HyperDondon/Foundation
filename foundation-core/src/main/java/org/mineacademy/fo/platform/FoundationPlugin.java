package org.mineacademy.fo.platform;

import java.io.File;

import org.mineacademy.fo.command.SimpleCommandCore;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.proxy.ProxyListener;

public interface FoundationPlugin {

	void disable();

	String getAuthors();

	File getDataFolder();

	SimpleCommandGroup getDefaultCommandGroup();

	ProxyListener getDefaultProxyListener();

	File getFile();

	int getFoundedYear();

	String getName();

	ClassLoader getPluginClassLoader();

	String getVersion();

	boolean isEnabled();

	boolean isRegexCaseInsensitive();

	boolean isRegexStrippingAccents();

	boolean isRegexStrippingColors();

	boolean isRegexUnicode();

	boolean isSimilarityStrippingAccents();

	void loadLibrary(String groupId, String artifactId, String version);

	void registerCommand(SimpleCommandCore instance);

	void registerCommands(SimpleCommandGroup group);

	void reload();

	void setDefaultCommandGroup(SimpleCommandGroup group);

	void setDefaultProxyListener(ProxyListener instance);
}
