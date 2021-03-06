package com.withinet.opaas.controller.system;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import com.withinet.opaas.model.domain.Bundle;

public interface BundleInstaller {
	
	public List<Bundle> installZip(String zipPath, String destPath) throws IOException, ParserConfigurationException;

	public Bundle installBundle(String jarPath, String destPath) throws IOException;

	public List<Bundle> installPom(String pomPath, String destPath) throws IOException, ParserConfigurationException;

	List<Bundle> installBundles(List<String> pomJarZipPath, String destPath)
			throws IOException, ParserConfigurationException;
}
