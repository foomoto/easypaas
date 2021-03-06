package com.withinet.opaas.controller.system.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
//import org.apache.maven.cli.MavenCli;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import com.withinet.opaas.controller.system.BundleInstaller;
import com.withinet.opaas.model.domain.Bundle;

@Service
public class BundleFileInstallerImpl implements BundleInstaller {
	
	@Override
	public List<Bundle> installBundles(List<String> pomJarZipPath, String destPath) throws IOException, ParserConfigurationException {
		List<Bundle> bundles = new ArrayList<Bundle> ();
		for (String fileName : pomJarZipPath) {
			fileName = fileName.toLowerCase().trim();
			String extension = FilenameUtils.getExtension(fileName);
			extension = extension.toLowerCase().trim();
			if (extension.equals("xml"))
				bundles.addAll(installPom (fileName, destPath));
			else if (extension.equals("zip"))
				bundles.addAll (installZip (fileName, destPath));
			else if (extension.equals("jar"))
				bundles.add(installBundle (fileName, destPath));
			else{
				new File (fileName).delete();
				throw new IOException ("Invalid extension submitted");
			}	
		}
		return bundles;
	}
	
	@Override
	public List<Bundle> installPom (String pomPath, String destPath) throws IOException, ParserConfigurationException {
		File pomFile = new File (pomPath);
		if (!pomFile.exists() || 
				pomFile.isDirectory() || !pomFile.getAbsolutePath().endsWith(".xml"))
			throw new IOException ("Destination path not pom file");
		try {
			validatePom (pomFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(pomFile);
			doc.getDocumentElement().normalize();
			NodeList dependencies = doc.getElementsByTagName("dependency");
			List<Bundle> bundles = new ArrayList <Bundle> ();
			for (int i = 0; i < dependencies.getLength(); i++) {
				Bundle bundle = new Bundle ();
				Node dependency = dependencies.item(i);
				Element e = (Element) dependency;
				StringBuffer buffer = new StringBuffer ();
				buffer.append("mvn:");
				String gid = ((Element) e.getElementsByTagName("groupId").item(0)).getTextContent();
				String aid = ((Element) e.getElementsByTagName("artifactId").item(0)).getTextContent();
				String version = null;
				buffer.append(gid + "/");
				buffer.append(aid);
				if (e.getElementsByTagName("version").item(0) instanceof Element && ((Element) e.getElementsByTagName("version").item(0)) != null) {
					version =((Element) e.getElementsByTagName("version").item(0)).getTextContent();
					buffer.append("/" + version);
				}
						
				
				bundle.setLocation(buffer.toString());
				
				if (version != null)
					bundle.setSymbolicName(gid + "-" + aid + "-" + version);
				else 
					bundle.setSymbolicName(gid + "-" + aid);
				
				bundles.add(bundle);
			}
			return bundles;
		} catch (SAXException e) {
			throw new IOException ("File not a valid maven pom file");
		}
		
	}
	
	@Override
	public List<Bundle> installZip (String zipPath, String destPath) throws IOException, ParserConfigurationException {
		File zipFile = new File (zipPath);
		File destPathDir = new File (destPath);
		if (!zipFile.exists()) throw new IOException ("File not found");
		if (!zipPath.endsWith(".zip")) throw new IOException ("Only Zip allowed here");
		if (!destPathDir.exists()) destPathDir.mkdirs();
		if (!destPathDir.isDirectory()) throw new IOException ("Destination path not directory");
		List<Bundle> bundles = new ArrayList <Bundle> ();
		FileInputStream fin = new FileInputStream(zipFile);
		BufferedInputStream bin = new BufferedInputStream(fin);
		ZipInputStream zin = new ZipInputStream(bin);
		ZipEntry ze = null;
		while ((ze = zin.getNextEntry()) != null) {
		    if (ze.getName().endsWith(".jar") || ze.getName().endsWith(".war")) {
		    	String fileLocation = new File (destPath + "/" + ze.getName()).getAbsolutePath();
		    	OutputStream out = new FileOutputStream(fileLocation);
		    	byte[] buffer = new byte[8192];
		        int len;
		        while ((len = zin.read(buffer)) != -1) {
		            out.write(buffer, 0, len);
		        }
		        out.close();
		        Bundle bundle = new Bundle ();
		        bundle.setLocation(fileLocation);
		        bundle.setSymbolicName(new File(fileLocation).getName());
		        bundles.add(bundle);
		    } else if (ze.getName().endsWith(".xml")) {
		    	String destFilePath = new File (destPath + "/" + ze.getName()).getAbsolutePath();
		    	OutputStream out = new FileOutputStream(destFilePath);
		    	byte[] buffer = new byte[8192];
		        int len;
		        while ((len = zin.read(buffer)) != -1) {
		            out.write(buffer, 0, len);
		        }
		        out.close();
		        out.flush();
		        List<Bundle> moreBundles = installPom (destFilePath, destPath);
		        bundles.addAll(moreBundles);
		    }
		}
		zin.close();
		bin.close();
		fin.close();
		return bundles;
	}
	
	@Override
	public Bundle installBundle (String bundleDest, String fileDest) throws IOException {
		if (!bundleDest.endsWith(".jar")) throw new IOException ("The file is not a valid bundle");
		
		File thisFile = new File (bundleDest);
		if (!thisFile.exists()) throw new IOException ("File does not exist");
		String newFileName = fileDest  + "/" + thisFile.getName();
		File thatFile = new File (newFileName);
		thisFile.renameTo(thatFile);
		Bundle bundle = new Bundle ();
		bundle.setLocation(thatFile.getAbsolutePath());
		bundle.setSymbolicName(thatFile.getName());
		return bundle;
	}
	
	private void validatePom (File filePath) throws SAXException, IOException {
		URL schemaFile;
		try {
			schemaFile = new URL("http://maven.apache.org/xsd/maven-4.0.0.xsd");
		} catch (MalformedURLException e) {
			throw new RuntimeException ("Server error " + e.getMessage());
		}
		Source xmlFile = new StreamSource(filePath);
		SchemaFactory schemaFactory = SchemaFactory
		    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(schemaFile);
		Validator validator = schema.newValidator();
		validator.validate(xmlFile);
	}
}
