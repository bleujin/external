package net.ion.cms.env;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import net.ion.framework.configuration.Configuration;
import net.ion.framework.configuration.ConfigurationException;
import net.ion.framework.configuration.NotFoundXmlTagException;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.ObjectId;
import net.ion.framework.util.PathMaker;
import net.ion.framework.util.StringUtil;
import net.ion.framework.vfs.FileSystemEntry;
import net.ion.framework.vfs.RenamePolicy;
import net.ion.framework.vfs.VFS;
import net.ion.framework.vfs.VFile;

import org.apache.commons.vfs2.FileSystemException;

public class ICSFileSystem {

	private FileSystemEntry fse;
	private final String icsRoot;
	private String thothWorkDir;

	private ICSFileSystem(FileSystemEntry fse, String icsRoot, String thothWorkDir) {
		this.fse = fse;
		this.icsRoot = icsRoot;
		this.thothWorkDir = thothWorkDir;
	}

	public static ICSFileSystem create(Configuration ifsConfig, String icsRoot) throws ConfigurationException, IOException {
		String base = ifsConfig.getAttribute("base");
		if (!(base.endsWith("/") || base.endsWith("\\"))) {
			base += "/";
		}

		try {
			String tmpPath = ifsConfig.getChild("temp").getValue();
			File tmpDir = new File(parseDirPath(base, tmpPath));
			IOUtil.setTempDir(tmpDir);
		} catch (NotFoundXmlTagException ignore) {
		}

		String thothWorkDir = "";
		try {
			thothWorkDir = (parseDirPath(base, ifsConfig.getChild("thoth").getValue()));
		} catch (NotFoundXmlTagException ignore) {
		}

		// Configuration vfConfig = ifsConfig.getChild("virtual-file-system");
		// StringBuilder sb = new StringBuilder("<providers>");
		// vfConfig.getChild("providers").getChildren("provider");
		// Configuration[] providers = vfConfig.getChildren("providers.provider");
		// for (Configuration provider : providers) {
		// sb.append("<provider class-name=\"").append(provider.getAttribute("class-name")).append("\">");
		// Configuration[] schemes = provider.getChildren("scheme");
		// for (Configuration scheme : schemes) {
		// sb.append("<scheme name=\"").append(scheme.getAttribute("name")).append("\"/>");
		// }
		// try {
		// Configuration[] propertys = provider.getChildren("property");
		// for (Configuration property : propertys) {
		// sb.append("<property name=\"").append(property.getAttribute("name")).append("\" ").append("value=\"").append(property.getAttribute("value")).append("\"/>");
		// }
		// } catch (NotFoundXmlTagException ignore) {
		// }
		// try {
		// Configuration[] availables = provider.getChildren("if-available");
		// for (Configuration available : availables) {
		// sb.append("<if-available class-name=\"").append(available.getAttribute("class-name")).append("\">");
		// }
		// } catch (NotFoundXmlTagException ignore) {
		// }
		// sb.append("</provider>");
		// }
		// sb.append("</providers>");

		StringBuilder sb = new StringBuilder();
		sb.append("<providers>\n");
		sb.append("    <provider class-name=\"net.ion.framework.vfs.LocalSubDirFileProvider\">\n");
		sb.append("        <property name=\"prefixDir\" value=\"").append(parseDirPath(base, ifsConfig.getChild("thumbnail").getValue())).append("\"/>\n");
		sb.append("        <scheme name=\"thumbnail\"/>\n");
		sb.append("    </provider>\n");
		sb.append("    <provider class-name=\"net.ion.framework.vfs.LocalSubDirFileProvider\">\n");
		sb.append("        <property name=\"prefixDir\" value=\"").append(parseDirPath(base, ifsConfig.getChild("artimage").getValue())).append("\"/>\n");
		sb.append("        <scheme name=\"artimage\"/>\n");
		sb.append("    </provider>\n");
		sb.append("    <provider class-name=\"net.ion.framework.vfs.LocalSubDirFileProvider\">\n");
		sb.append("        <property name=\"prefixDir\" value=\"").append(parseDirPath(base, ifsConfig.getChild("afield").getValue())).append("\"/>\n");
		sb.append("        <scheme name=\"afield\"/>\n");
		sb.append("    </provider>\n");
		sb.append("    <provider class-name=\"net.ion.framework.vfs.LocalSubDirFileProvider\">\n");
		sb.append("        <property name=\"prefixDir\" value=\"").append(parseDirPath(base, ifsConfig.getChild("meta").getValue())).append("\"/>\n");
		sb.append("        <scheme name=\"meta\"/>\n");
		sb.append("    </provider>\n");
		sb.append("    <provider class-name=\"net.ion.framework.vfs.LocalSubDirFileProvider\">\n");
		sb.append("        <property name=\"prefixDir\" value=\"").append(parseDirPath(base, ifsConfig.getChild("resource").getValue())).append("\"/>\n");
		sb.append("        <scheme name=\"resource\"/>\n");
		sb.append("    </provider>\n");
		sb.append("</providers>\n");

		File file = ICSFileSystem.createTempFile("vfs_config");
		FileWriter writer = new FileWriter(file);
		writer.write(sb.toString());
		writer.close();

		FileSystemEntry fse = VFS.getManger(file.toURI().toURL());
		file.delete();

		return new ICSFileSystem(fse, icsRoot, thothWorkDir);
	}

	private static String parseDirPath(String base, String dirPath) {
		if (!(dirPath.startsWith("/") || dirPath.startsWith("\\") || dirPath.indexOf(":") == 1)) {
			if (dirPath.startsWith("./") || dirPath.startsWith(".\\")) {
				dirPath = dirPath.substring("./".length());
			}
			dirPath = base + dirPath;
		}
		return dirPath;
	}

	// private static LocalSubDirFileProvider makeProvider(String prefixDir) {
	// LocalSubDirFileProvider afield = new LocalSubDirFileProvider();
	// afield.setPrefixDir(prefixDir);
	// return afield;
	// }

	public VFile thumbnailFile(String... names) throws FileSystemException {
		return fse.resolveFile("thumbnail:/" + makePath(names));
	}

	public VFile artimageFile(String... names) throws FileSystemException {
		return fse.resolveFile("artimage:/" + makePath(names));
	}

	public VFile afieldFile(String... names) throws FileSystemException {
		return fse.resolveFile("afield:/" + makePath(names));
	}

	public VFile resourceFile(String... names) throws FileSystemException {
		return fse.resolveFile("resource:/" + makePath(names));
	}

	public VFile metaFile(String... names) throws FileSystemException {
		return fse.resolveFile("meta:/" + makePath(names));
	}

	public VFile write(InputStream input, VFile vf, RenamePolicy policy) throws IOException {
		return fse.write(input, vf, policy);
	}

	public VFile getEnableVFile(final VFile f) throws FileSystemException {
		VFile newFile = f;
		if (isEnableFileName(newFile))
			return newFile;
		String name = newFile.getName().getBaseName();
		String body = StringUtil.substringBeforeLast(name, ".");
		String ext = StringUtil.defaultIfEmpty(StringUtil.substringAfterLast(name, "."), "");
		if (StringUtil.isNotBlank(ext)) {
			ext = "." + ext;
		}

		for (int count = 0; !isEnableFileName(newFile) && count < 9999;) {
			count++;
			String newBaseName = body + "_" + count + ext;

			newFile = fse.resolveFile(f.getName().getScheme() + ":" + f.getParent().getName().getPath() + "/" + newBaseName);
		}

		return newFile;
	}

	private boolean isEnableFileName(VFile f) {
		try {
			if (f.exists())
				return false;
			f.createFile();
			return true;
		} catch (FileSystemException ignored) {
			return false;
		}
	}

//	// ibr_17598, ibr_17599
//	@SuppressWarnings("deprecation")
//	private String makePath(String... names) {
//		String tempDir = StringUtil.join(names, "/");
//		tempDir = tempDir.replaceAll("\\\\", "/");
//		String path[] = StringUtil.split(tempDir, "/");
//		// return (names[0].startsWith("/") ? "/" : "") + StringUtil.join(path, "/");
//		String filePath = (names[0].startsWith("/") ? "/" : "") + StringUtil.join(path, "/");
//		try {
//			filePath = URLEncoder.encode(filePath, "utf-8");
//		} catch (UnsupportedEncodingException e) {
//			Debug.info(e.getLocalizedMessage());
//			filePath = URLEncoder.encode(filePath);
//		}
//		return StringUtil.replace(filePath, "+", "%20");
//	}
	
	//ibr_17598, ibr_17599, ibr_17682, by_novision
	@SuppressWarnings("deprecation")
	private String makePath(String... names) {
		String tempDir = StringUtil.join(names, "/");
		tempDir = tempDir.replaceAll("\\\\", "/");
		String path[] = StringUtil.split(tempDir, "/");
		String filePath = (names[0].startsWith("/") ? "/" : "") + StringUtil.join(path, "/");
		try {
			filePath = URLEncoder.encode(filePath, "utf-8");
		} catch (UnsupportedEncodingException e) {
			Debug.info(e.getLocalizedMessage());
			filePath = URLEncoder.encode(filePath);
		}
		filePath = StringUtil.replace(filePath, "+", "%20");
		return StringUtil.replace(filePath, "%2B", "+");
	}
	
	public VFile resoloveFile(String scheme, String... names) throws FileSystemException {
		return fse.resolveFile(scheme + ":/" + makePath(names));
	}

	public String getWebRoot() {
		return icsRoot;
	}

	public String findICSConfigFile(String subPath) {
		return PathMaker.getFilePath(getWebRoot(), subPath);
	}

	public static File createTempFile(String prefix) throws IOException {
		return IOUtil.createTempFile("ics_" + prefix);
	}

	public static File createTempFile(String prefix, String ext) throws IOException {
		return File.createTempFile("ics_" + prefix + ObjectId.get().toString(), "." + ext, getTempDir());
	}

	public static File getTempDir() {
		return IOUtil.getTempDir();
	}

	public FileSystemEntry getFileSystemEntry() {
		return fse;
	}

	public String getThothWorkDir() {
		if (StringUtil.isEmpty(thothWorkDir)) {
			thothWorkDir = PathMaker.getFilePath(icsRoot, "/uploadfiles/thoth/");
		}
		return thothWorkDir;
	}
}
