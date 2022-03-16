package com.gdxsoft.easyweb.script.servlets;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.Utils;

public class FileOut {
	public static Map<String, String> MAP = new ConcurrentHashMap<String, String>();
	public static String DEF_DOWNLOAD_TYPE = "application/octet-stream";
	static {
		MAP.put("123", "application/vnd.lotus-1-2-3");
		MAP.put("3dml", "text/vnd.in3d.3dml");
		MAP.put("3ds", "image/x-3ds");
		MAP.put("3g2", "video/3gpp2");
		MAP.put("3gp", "video/3gpp");
		MAP.put("7z", "application/x-7z-compressed");
		MAP.put("aab", "application/x-authorware-bin");
		MAP.put("aac", "audio/x-aac");
		MAP.put("aam", "application/x-authorware-map");
		MAP.put("aas", "application/x-authorware-seg");
		MAP.put("abs", "audio/x-mpeg");
		MAP.put("abw", "application/x-abiword");
		MAP.put("ac", "application/pkix-attr-cert");
		MAP.put("acc", "application/vnd.americandynamics.acc");
		MAP.put("ace", "application/x-ace-compressed");
		MAP.put("acu", "application/vnd.acucobol");
		MAP.put("acutc", "application/vnd.acucorp");
		MAP.put("adp", "audio/adpcm");
		MAP.put("aep", "application/vnd.audiograph");
		MAP.put("afm", "application/x-font-type1");
		MAP.put("afp", "application/vnd.ibm.modcap");
		MAP.put("ahead", "application/vnd.ahead.space");
		MAP.put("ai", "application/postscript");
		MAP.put("aif", "audio/x-aiff");
		MAP.put("aifc", "audio/x-aiff");
		MAP.put("aiff", "audio/x-aiff");
		MAP.put("aim", "application/x-aim");
		MAP.put("air", "application/vnd.adobe.air-application-installer-package+zip");
		MAP.put("ait", "application/vnd.dvb.ait");
		MAP.put("ami", "application/vnd.amiga.ami");
		MAP.put("anx", "application/annodex");
		MAP.put("apk", "application/vnd.android.package-archive");
		MAP.put("appcache", "text/cache-manifest");
		MAP.put("application", "application/x-ms-application");
		MAP.put("apr", "application/vnd.lotus-approach");
		MAP.put("arc", "application/x-freearc");
		MAP.put("art", "image/x-jg");
		MAP.put("asc", "application/pgp-signature");
		MAP.put("asf", "video/x-ms-asf");
		MAP.put("asm", "text/x-asm");
		MAP.put("aso", "application/vnd.accpac.simply.aso");
		MAP.put("asx", "video/x-ms-asf");
		MAP.put("atc", "application/vnd.acucorp");
		MAP.put("atom", "application/atom+xml");
		MAP.put("atomcat", "application/atomcat+xml");
		MAP.put("atomsvc", "application/atomsvc+xml");
		MAP.put("atx", "application/vnd.antix.game-component");
		MAP.put("au", "audio/basic");
		MAP.put("avi", "video/x-msvideo");
		MAP.put("avx", "video/x-rad-screenplay");
		MAP.put("aw", "application/applixware");
		MAP.put("axa", "audio/annodex");
		MAP.put("axv", "video/annodex");
		MAP.put("azf", "application/vnd.airzip.filesecure.azf");
		MAP.put("azs", "application/vnd.airzip.filesecure.azs");
		MAP.put("azw", "application/vnd.amazon.ebook");
		MAP.put("bat", "application/x-msdownload");
		MAP.put("bcpio", "application/x-bcpio");
		MAP.put("bdf", "application/x-font-bdf");
		MAP.put("bdm", "application/vnd.syncml.dm+wbxml");
		MAP.put("bed", "application/vnd.realvnc.bed");
		MAP.put("bh2", "application/vnd.fujitsu.oasysprs");
		MAP.put("bin", "application/octet-stream");
		MAP.put("blb", "application/x-blorb");
		MAP.put("blorb", "application/x-blorb");
		MAP.put("bmi", "application/vnd.bmi");
		MAP.put("bmp", "image/bmp");
		MAP.put("body", "text/html");
		MAP.put("book", "application/vnd.framemaker");
		MAP.put("box", "application/vnd.previewsystems.box");
		MAP.put("boz", "application/x-bzip2");
		MAP.put("bpk", "application/octet-stream");
		MAP.put("btif", "image/prs.btif");
		MAP.put("bz", "application/x-bzip");
		MAP.put("bz2", "application/x-bzip2");
		MAP.put("c", "text/x-c");
		MAP.put("c11amc", "application/vnd.cluetrust.cartomobile-config");
		MAP.put("c11amz", "application/vnd.cluetrust.cartomobile-config-pkg");
		MAP.put("c4d", "application/vnd.clonk.c4group");
		MAP.put("c4f", "application/vnd.clonk.c4group");
		MAP.put("c4g", "application/vnd.clonk.c4group");
		MAP.put("c4p", "application/vnd.clonk.c4group");
		MAP.put("c4u", "application/vnd.clonk.c4group");
		MAP.put("cab", "application/vnd.ms-cab-compressed");
		MAP.put("caf", "audio/x-caf");
		MAP.put("cap", "application/vnd.tcpdump.pcap");
		MAP.put("car", "application/vnd.curl.car");
		MAP.put("cat", "application/vnd.ms-pki.seccat");
		MAP.put("cb7", "application/x-cbr");
		MAP.put("cba", "application/x-cbr");
		MAP.put("cbr", "application/x-cbr");
		MAP.put("cbt", "application/x-cbr");
		MAP.put("cbz", "application/x-cbr");
		MAP.put("cc", "text/x-c");
		MAP.put("cct", "application/x-director");
		MAP.put("ccxml", "application/ccxml+xml");
		MAP.put("cdbcmsg", "application/vnd.contact.cmsg");
		MAP.put("cdf", "application/x-cdf");
		MAP.put("cdkey", "application/vnd.mediastation.cdkey");
		MAP.put("cdmia", "application/cdmi-capability");
		MAP.put("cdmic", "application/cdmi-container");
		MAP.put("cdmid", "application/cdmi-domain");
		MAP.put("cdmio", "application/cdmi-object");
		MAP.put("cdmiq", "application/cdmi-queue");
		MAP.put("cdx", "chemical/x-cdx");
		MAP.put("cdxml", "application/vnd.chemdraw+xml");
		MAP.put("cdy", "application/vnd.cinderella");
		MAP.put("cer", "application/pkix-cert");
		MAP.put("cfs", "application/x-cfs-compressed");
		MAP.put("cgm", "image/cgm");
		MAP.put("chat", "application/x-chat");
		MAP.put("chm", "application/vnd.ms-htmlhelp");
		MAP.put("chrt", "application/vnd.kde.kchart");
		MAP.put("cif", "chemical/x-cif");
		MAP.put("cii", "application/vnd.anser-web-certificate-issue-initiation");
		MAP.put("cil", "application/vnd.ms-artgalry");
		MAP.put("cla", "application/vnd.claymore");
		MAP.put("class", "application/java");
		MAP.put("clkk", "application/vnd.crick.clicker.keyboard");
		MAP.put("clkp", "application/vnd.crick.clicker.palette");
		MAP.put("clkt", "application/vnd.crick.clicker.template");
		MAP.put("clkw", "application/vnd.crick.clicker.wordbank");
		MAP.put("clkx", "application/vnd.crick.clicker");
		MAP.put("clp", "application/x-msclip");
		MAP.put("cmc", "application/vnd.cosmocaller");
		MAP.put("cmdf", "chemical/x-cmdf");
		MAP.put("cml", "chemical/x-cml");
		MAP.put("cmp", "application/vnd.yellowriver-custom-menu");
		MAP.put("cmx", "image/x-cmx");
		MAP.put("cod", "application/vnd.rim.cod");
		MAP.put("com", "application/x-msdownload");
		MAP.put("conf", "text/plain");
		MAP.put("cpio", "application/x-cpio");
		MAP.put("cpp", "text/x-c");
		MAP.put("cpt", "application/mac-compactpro");
		MAP.put("crd", "application/x-mscardfile");
		MAP.put("crl", "application/pkix-crl");
		MAP.put("crt", "application/x-x509-ca-cert");
		MAP.put("cryptonote", "application/vnd.rig.cryptonote");
		MAP.put("csh", "application/x-csh");
		MAP.put("csml", "chemical/x-csml");
		MAP.put("csp", "application/vnd.commonspace");
		MAP.put("css", "text/css");
		MAP.put("cst", "application/x-director");
		MAP.put("csv", "text/csv");
		MAP.put("cu", "application/cu-seeme");
		MAP.put("curl", "text/vnd.curl");
		MAP.put("cww", "application/prs.cww");
		MAP.put("cxt", "application/x-director");
		MAP.put("cxx", "text/x-c");
		MAP.put("dae", "model/vnd.collada+xml");
		MAP.put("daf", "application/vnd.mobius.daf");
		MAP.put("dart", "application/vnd.dart");
		MAP.put("dataless", "application/vnd.fdsn.seed");
		MAP.put("davmount", "application/davmount+xml");
		MAP.put("dbk", "application/docbook+xml");
		MAP.put("dcr", "application/x-director");
		MAP.put("dcurl", "text/vnd.curl.dcurl");
		MAP.put("dd2", "application/vnd.oma.dd2+xml");
		MAP.put("ddd", "application/vnd.fujixerox.ddd");
		MAP.put("deb", "application/x-debian-package");
		MAP.put("def", "text/plain");
		MAP.put("deploy", "application/octet-stream");
		MAP.put("der", "application/x-x509-ca-cert");
		MAP.put("dfac", "application/vnd.dreamfactory");
		MAP.put("dgc", "application/x-dgc-compressed");
		MAP.put("dib", "image/bmp");
		MAP.put("dic", "text/x-c");
		MAP.put("dir", "application/x-director");
		MAP.put("dis", "application/vnd.mobius.dis");
		MAP.put("dist", "application/octet-stream");
		MAP.put("distz", "application/octet-stream");
		MAP.put("djv", "image/vnd.djvu");
		MAP.put("djvu", "image/vnd.djvu");
		MAP.put("dll", "application/x-msdownload");
		MAP.put("dmg", "application/x-apple-diskimage");
		MAP.put("dmp", "application/vnd.tcpdump.pcap");
		MAP.put("dms", "application/octet-stream");
		MAP.put("dna", "application/vnd.dna");
		MAP.put("doc", "application/msword");
		MAP.put("docm", "application/vnd.ms-word.document.macroenabled.12");
		MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		MAP.put("dot", "application/msword");
		MAP.put("dotm", "application/vnd.ms-word.template.macroenabled.12");
		MAP.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
		MAP.put("dp", "application/vnd.osgi.dp");
		MAP.put("dpg", "application/vnd.dpgraph");
		MAP.put("dra", "audio/vnd.dra");
		MAP.put("dsc", "text/prs.lines.tag");
		MAP.put("dssc", "application/dssc+der");
		MAP.put("dtb", "application/x-dtbook+xml");
		MAP.put("dtd", "application/xml-dtd");
		MAP.put("dts", "audio/vnd.dts");
		MAP.put("dtshd", "audio/vnd.dts.hd");
		MAP.put("dump", "application/octet-stream");
		MAP.put("dv", "video/x-dv");
		MAP.put("dvb", "video/vnd.dvb.file");
		MAP.put("dvi", "application/x-dvi");
		MAP.put("dwf", "model/vnd.dwf");
		MAP.put("dwg", "image/vnd.dwg");
		MAP.put("dxf", "image/vnd.dxf");
		MAP.put("dxp", "application/vnd.spotfire.dxp");
		MAP.put("dxr", "application/x-director");
		MAP.put("ecelp4800", "audio/vnd.nuera.ecelp4800");
		MAP.put("ecelp7470", "audio/vnd.nuera.ecelp7470");
		MAP.put("ecelp9600", "audio/vnd.nuera.ecelp9600");
		MAP.put("ecma", "application/ecmascript");
		MAP.put("edm", "application/vnd.novadigm.edm");
		MAP.put("edx", "application/vnd.novadigm.edx");
		MAP.put("efif", "application/vnd.picsel");
		MAP.put("ei6", "application/vnd.pg.osasli");
		MAP.put("elc", "application/octet-stream");
		MAP.put("emf", "application/x-msmetafile");
		MAP.put("eml", "message/rfc822");
		MAP.put("emma", "application/emma+xml");
		MAP.put("emz", "application/x-msmetafile");
		MAP.put("eol", "audio/vnd.digital-winds");
		MAP.put("eot", "application/vnd.ms-fontobject");
		MAP.put("eps", "application/postscript");
		MAP.put("epub", "application/epub+zip");
		MAP.put("es3", "application/vnd.eszigno3+xml");
		MAP.put("esa", "application/vnd.osgi.subsystem");
		MAP.put("esf", "application/vnd.epson.esf");
		MAP.put("et3", "application/vnd.eszigno3+xml");
		MAP.put("etx", "text/x-setext");
		MAP.put("eva", "application/x-eva");
		MAP.put("evy", "application/x-envoy");
		MAP.put("exe", "application/octet-stream");
		MAP.put("exi", "application/exi");
		MAP.put("ext", "application/vnd.novadigm.ext");
		MAP.put("ez", "application/andrew-inset");
		MAP.put("ez2", "application/vnd.ezpix-album");
		MAP.put("ez3", "application/vnd.ezpix-package");
		MAP.put("f", "text/x-fortran");
		MAP.put("f4v", "video/x-f4v");
		MAP.put("f77", "text/x-fortran");
		MAP.put("f90", "text/x-fortran");
		MAP.put("fbs", "image/vnd.fastbidsheet");
		MAP.put("fcdt", "application/vnd.adobe.formscentral.fcdt");
		MAP.put("fcs", "application/vnd.isac.fcs");
		MAP.put("fdf", "application/vnd.fdf");
		MAP.put("fe_launch", "application/vnd.denovo.fcselayout-link");
		MAP.put("fg5", "application/vnd.fujitsu.oasysgp");
		MAP.put("fgd", "application/x-director");
		MAP.put("fh", "image/x-freehand");
		MAP.put("fh4", "image/x-freehand");
		MAP.put("fh5", "image/x-freehand");
		MAP.put("fh7", "image/x-freehand");
		MAP.put("fhc", "image/x-freehand");
		MAP.put("fig", "application/x-xfig");
		MAP.put("flac", "audio/flac");
		MAP.put("fli", "video/x-fli");
		MAP.put("flo", "application/vnd.micrografx.flo");
		MAP.put("flv", "video/x-flv");
		MAP.put("flw", "application/vnd.kde.kivio");
		MAP.put("flx", "text/vnd.fmi.flexstor");
		MAP.put("fly", "text/vnd.fly");
		MAP.put("fm", "application/vnd.framemaker");
		MAP.put("fnc", "application/vnd.frogans.fnc");
		MAP.put("for", "text/x-fortran");
		MAP.put("fpx", "image/vnd.fpx");
		MAP.put("frame", "application/vnd.framemaker");
		MAP.put("fsc", "application/vnd.fsc.weblaunch");
		MAP.put("fst", "image/vnd.fst");
		MAP.put("ftc", "application/vnd.fluxtime.clip");
		MAP.put("fti", "application/vnd.anser-web-funds-transfer-initiation");
		MAP.put("fvt", "video/vnd.fvt");
		MAP.put("fxp", "application/vnd.adobe.fxp");
		MAP.put("fxpl", "application/vnd.adobe.fxp");
		MAP.put("fzs", "application/vnd.fuzzysheet");
		MAP.put("g2w", "application/vnd.geoplan");
		MAP.put("g3", "image/g3fax");
		MAP.put("g3w", "application/vnd.geospace");
		MAP.put("gac", "application/vnd.groove-account");
		MAP.put("gam", "application/x-tads");
		MAP.put("gbr", "application/rpki-ghostbusters");
		MAP.put("gca", "application/x-gca-compressed");
		MAP.put("gdl", "model/vnd.gdl");
		MAP.put("geo", "application/vnd.dynageo");
		MAP.put("gex", "application/vnd.geometry-explorer");
		MAP.put("ggb", "application/vnd.geogebra.file");
		MAP.put("ggt", "application/vnd.geogebra.tool");
		MAP.put("ghf", "application/vnd.groove-help");
		MAP.put("gif", "image/gif");
		MAP.put("gim", "application/vnd.groove-identity-message");
		MAP.put("gml", "application/gml+xml");
		MAP.put("gmx", "application/vnd.gmx");
		MAP.put("gnumeric", "application/x-gnumeric");
		MAP.put("gph", "application/vnd.flographit");
		MAP.put("gpx", "application/gpx+xml");
		MAP.put("gqf", "application/vnd.grafeq");
		MAP.put("gqs", "application/vnd.grafeq");
		MAP.put("gram", "application/srgs");
		MAP.put("gramps", "application/x-gramps-xml");
		MAP.put("gre", "application/vnd.geometry-explorer");
		MAP.put("grv", "application/vnd.groove-injector");
		MAP.put("grxml", "application/srgs+xml");
		MAP.put("gsf", "application/x-font-ghostscript");
		MAP.put("gtar", "application/x-gtar");
		MAP.put("gtm", "application/vnd.groove-tool-message");
		MAP.put("gtw", "model/vnd.gtw");
		MAP.put("gv", "text/vnd.graphviz");
		MAP.put("gxf", "application/gxf");
		MAP.put("gxt", "application/vnd.geonext");
		MAP.put("gz", "application/x-gzip");
		MAP.put("h", "text/x-c");
		MAP.put("h261", "video/h261");
		MAP.put("h263", "video/h263");
		MAP.put("h264", "video/h264");
		MAP.put("hal", "application/vnd.hal+xml");
		MAP.put("hbci", "application/vnd.hbci");
		MAP.put("hdf", "application/x-hdf");
		MAP.put("hh", "text/x-c");
		MAP.put("hlp", "application/winhlp");
		MAP.put("hpgl", "application/vnd.hp-hpgl");
		MAP.put("hpid", "application/vnd.hp-hpid");
		MAP.put("hps", "application/vnd.hp-hps");
		MAP.put("hqx", "application/mac-binhex40");
		MAP.put("htc", "text/x-component");
		MAP.put("htke", "application/vnd.kenameaapp");
		MAP.put("htm", "text/html");
		MAP.put("html", "text/html");
		MAP.put("hvd", "application/vnd.yamaha.hv-dic");
		MAP.put("hvp", "application/vnd.yamaha.hv-voice");
		MAP.put("hvs", "application/vnd.yamaha.hv-script");
		MAP.put("i2g", "application/vnd.intergeo");
		MAP.put("icc", "application/vnd.iccprofile");
		MAP.put("ice", "x-conference/x-cooltalk");
		MAP.put("icm", "application/vnd.iccprofile");
		MAP.put("ico", "image/x-icon");
		MAP.put("ics", "text/calendar");
		MAP.put("ief", "image/ief");
		MAP.put("ifb", "text/calendar");
		MAP.put("ifm", "application/vnd.shana.informed.formdata");
		MAP.put("iges", "model/iges");
		MAP.put("igl", "application/vnd.igloader");
		MAP.put("igm", "application/vnd.insors.igm");
		MAP.put("igs", "model/iges");
		MAP.put("igx", "application/vnd.micrografx.igx");
		MAP.put("iif", "application/vnd.shana.informed.interchange");
		MAP.put("imp", "application/vnd.accpac.simply.imp");
		MAP.put("ims", "application/vnd.ms-ims");
		MAP.put("in", "text/plain");
		MAP.put("ink", "application/inkml+xml");
		MAP.put("inkml", "application/inkml+xml");
		MAP.put("install", "application/x-install-instructions");
		MAP.put("iota", "application/vnd.astraea-software.iota");
		MAP.put("ipfix", "application/ipfix");
		MAP.put("ipk", "application/vnd.shana.informed.package");
		MAP.put("irm", "application/vnd.ibm.rights-management");
		MAP.put("irp", "application/vnd.irepository.package+xml");
		MAP.put("iso", "application/x-iso9660-image");
		MAP.put("itp", "application/vnd.shana.informed.formtemplate");
		MAP.put("ivp", "application/vnd.immervision-ivp");
		MAP.put("ivu", "application/vnd.immervision-ivu");
		MAP.put("jad", "text/vnd.sun.j2me.app-descriptor");
		MAP.put("jam", "application/vnd.jam");
		MAP.put("jar", "application/java-archive");
		MAP.put("java", "text/x-java-source");
		MAP.put("jisp", "application/vnd.jisp");
		MAP.put("jlt", "application/vnd.hp-jlyt");
		MAP.put("jnlp", "application/x-java-jnlp-file");
		MAP.put("joda", "application/vnd.joost.joda-archive");
		MAP.put("jpe", "image/jpeg");
		MAP.put("jpeg", "image/jpeg");
		MAP.put("jpg", "image/jpeg");
		MAP.put("jpgm", "video/jpm");
		MAP.put("jpgv", "video/jpeg");
		MAP.put("jpm", "video/jpm");
		MAP.put("js", "application/javascript");
		MAP.put("jsf", "text/plain");
		MAP.put("json", "application/json");
		MAP.put("jsonml", "application/jsonml+json");
		MAP.put("jspf", "text/plain");
		MAP.put("kar", "audio/midi");
		MAP.put("karbon", "application/vnd.kde.karbon");
		MAP.put("kfo", "application/vnd.kde.kformula");
		MAP.put("kia", "application/vnd.kidspiration");
		MAP.put("kml", "application/vnd.google-earth.kml+xml");
		MAP.put("kmz", "application/vnd.google-earth.kmz");
		MAP.put("kne", "application/vnd.kinar");
		MAP.put("knp", "application/vnd.kinar");
		MAP.put("kon", "application/vnd.kde.kontour");
		MAP.put("kpr", "application/vnd.kde.kpresenter");
		MAP.put("kpt", "application/vnd.kde.kpresenter");
		MAP.put("kpxx", "application/vnd.ds-keypoint");
		MAP.put("ksp", "application/vnd.kde.kspread");
		MAP.put("ktr", "application/vnd.kahootz");
		MAP.put("ktx", "image/ktx");
		MAP.put("ktz", "application/vnd.kahootz");
		MAP.put("kwd", "application/vnd.kde.kword");
		MAP.put("kwt", "application/vnd.kde.kword");
		MAP.put("lasxml", "application/vnd.las.las+xml");
		MAP.put("latex", "application/x-latex");
		MAP.put("lbd", "application/vnd.llamagraphics.life-balance.desktop");
		MAP.put("lbe", "application/vnd.llamagraphics.life-balance.exchange+xml");
		MAP.put("les", "application/vnd.hhe.lesson-player");
		MAP.put("lha", "application/x-lzh-compressed");
		MAP.put("link66", "application/vnd.route66.link66+xml");
		MAP.put("list", "text/plain");
		MAP.put("list3820", "application/vnd.ibm.modcap");
		MAP.put("listafp", "application/vnd.ibm.modcap");
		MAP.put("lnk", "application/x-ms-shortcut");
		MAP.put("log", "text/plain");
		MAP.put("lostxml", "application/lost+xml");
		MAP.put("lrf", "application/octet-stream");
		MAP.put("lrm", "application/vnd.ms-lrm");
		MAP.put("ltf", "application/vnd.frogans.ltf");
		MAP.put("lvp", "audio/vnd.lucent.voice");
		MAP.put("lwp", "application/vnd.lotus-wordpro");
		MAP.put("lzh", "application/x-lzh-compressed");
		MAP.put("m13", "application/x-msmediaview");
		MAP.put("m14", "application/x-msmediaview");
		MAP.put("m1v", "video/mpeg");
		MAP.put("m21", "application/mp21");
		MAP.put("m2a", "audio/mpeg");
		MAP.put("m2v", "video/mpeg");
		MAP.put("m3a", "audio/mpeg");
		MAP.put("m3u", "audio/x-mpegurl");
		MAP.put("m3u8", "application/vnd.apple.mpegurl");
		MAP.put("m4a", "audio/mp4");
		MAP.put("m4b", "audio/mp4");
		MAP.put("m4r", "audio/mp4");
		MAP.put("m4u", "video/vnd.mpegurl");
		MAP.put("m4v", "video/mp4");
		MAP.put("ma", "application/mathematica");
		MAP.put("mac", "image/x-macpaint");
		MAP.put("mads", "application/mads+xml");
		MAP.put("mag", "application/vnd.ecowin.chart");
		MAP.put("maker", "application/vnd.framemaker");
		MAP.put("man", "text/troff");
		MAP.put("mar", "application/octet-stream");
		MAP.put("mathml", "application/mathml+xml");
		MAP.put("mb", "application/mathematica");
		MAP.put("mbk", "application/vnd.mobius.mbk");
		MAP.put("mbox", "application/mbox");
		MAP.put("mc1", "application/vnd.medcalcdata");
		MAP.put("mcd", "application/vnd.mcd");
		MAP.put("mcurl", "text/vnd.curl.mcurl");
		MAP.put("mdb", "application/x-msaccess");
		MAP.put("mdi", "image/vnd.ms-modi");
		MAP.put("me", "text/troff");
		MAP.put("mesh", "model/mesh");
		MAP.put("meta4", "application/metalink4+xml");
		MAP.put("metalink", "application/metalink+xml");
		MAP.put("mets", "application/mets+xml");
		MAP.put("mfm", "application/vnd.mfmp");
		MAP.put("mft", "application/rpki-manifest");
		MAP.put("mgp", "application/vnd.osgeo.mapguide.package");
		MAP.put("mgz", "application/vnd.proteus.magazine");
		MAP.put("mid", "audio/midi");
		MAP.put("midi", "audio/midi");
		MAP.put("mie", "application/x-mie");
		MAP.put("mif", "application/x-mif");
		MAP.put("mime", "message/rfc822");
		MAP.put("mj2", "video/mj2");
		MAP.put("mjp2", "video/mj2");
		MAP.put("mk3d", "video/x-matroska");
		MAP.put("mka", "audio/x-matroska");
		MAP.put("mks", "video/x-matroska");
		MAP.put("mkv", "video/x-matroska");
		MAP.put("mlp", "application/vnd.dolby.mlp");
		MAP.put("mmd", "application/vnd.chipnuts.karaoke-mmd");
		MAP.put("mmf", "application/vnd.smaf");
		MAP.put("mmr", "image/vnd.fujixerox.edmics-mmr");
		MAP.put("mng", "video/x-mng");
		MAP.put("mny", "application/x-msmoney");
		MAP.put("mobi", "application/x-mobipocket-ebook");
		MAP.put("mods", "application/mods+xml");
		MAP.put("mov", "video/quicktime");
		MAP.put("movie", "video/x-sgi-movie");
		MAP.put("mp1", "audio/mpeg");
		MAP.put("mp2", "audio/mpeg");
		MAP.put("mp21", "application/mp21");
		MAP.put("mp2a", "audio/mpeg");
		MAP.put("mp3", "audio/mpeg");
		MAP.put("mp4", "video/mp4");
		MAP.put("mp4a", "audio/mp4");
		MAP.put("mp4s", "application/mp4");
		MAP.put("mp4v", "video/mp4");
		MAP.put("mpa", "audio/mpeg");
		MAP.put("mpc", "application/vnd.mophun.certificate");
		MAP.put("mpe", "video/mpeg");
		MAP.put("mpeg", "video/mpeg");
		MAP.put("mpega", "audio/x-mpeg");
		MAP.put("mpg", "video/mpeg");
		MAP.put("mpg4", "video/mp4");
		MAP.put("mpga", "audio/mpeg");
		MAP.put("mpkg", "application/vnd.apple.installer+xml");
		MAP.put("mpm", "application/vnd.blueice.multipass");
		MAP.put("mpn", "application/vnd.mophun.application");
		MAP.put("mpp", "application/vnd.ms-project");
		MAP.put("mpt", "application/vnd.ms-project");
		MAP.put("mpv2", "video/mpeg2");
		MAP.put("mpy", "application/vnd.ibm.minipay");
		MAP.put("mqy", "application/vnd.mobius.mqy");
		MAP.put("mrc", "application/marc");
		MAP.put("mrcx", "application/marcxml+xml");
		MAP.put("ms", "text/troff");
		MAP.put("mscml", "application/mediaservercontrol+xml");
		MAP.put("mseed", "application/vnd.fdsn.mseed");
		MAP.put("mseq", "application/vnd.mseq");
		MAP.put("msf", "application/vnd.epson.msf");
		MAP.put("msh", "model/mesh");
		MAP.put("msi", "application/x-msdownload");
		MAP.put("msl", "application/vnd.mobius.msl");
		MAP.put("msty", "application/vnd.muvee.style");
		MAP.put("mts", "model/vnd.mts");
		MAP.put("mus", "application/vnd.musician");
		MAP.put("musicxml", "application/vnd.recordare.musicxml+xml");
		MAP.put("mvb", "application/x-msmediaview");
		MAP.put("mwf", "application/vnd.mfer");
		MAP.put("mxf", "application/mxf");
		MAP.put("mxl", "application/vnd.recordare.musicxml");
		MAP.put("mxml", "application/xv+xml");
		MAP.put("mxs", "application/vnd.triscape.mxs");
		MAP.put("mxu", "video/vnd.mpegurl");
		MAP.put("n-gage", "application/vnd.nokia.n-gage.symbian.install");
		MAP.put("n3", "text/n3");
		MAP.put("nb", "application/mathematica");
		MAP.put("nbp", "application/vnd.wolfram.player");
		MAP.put("nc", "application/x-netcdf");
		MAP.put("ncx", "application/x-dtbncx+xml");
		MAP.put("nfo", "text/x-nfo");
		MAP.put("ngdat", "application/vnd.nokia.n-gage.data");
		MAP.put("nitf", "application/vnd.nitf");
		MAP.put("nlu", "application/vnd.neurolanguage.nlu");
		MAP.put("nml", "application/vnd.enliven");
		MAP.put("nnd", "application/vnd.noblenet-directory");
		MAP.put("nns", "application/vnd.noblenet-sealer");
		MAP.put("nnw", "application/vnd.noblenet-web");
		MAP.put("npx", "image/vnd.net-fpx");
		MAP.put("nsc", "application/x-conference");
		MAP.put("nsf", "application/vnd.lotus-notes");
		MAP.put("ntf", "application/vnd.nitf");
		MAP.put("nzb", "application/x-nzb");
		MAP.put("oa2", "application/vnd.fujitsu.oasys2");
		MAP.put("oa3", "application/vnd.fujitsu.oasys3");
		MAP.put("oas", "application/vnd.fujitsu.oasys");
		MAP.put("obd", "application/x-msbinder");
		MAP.put("obj", "application/x-tgif");
		MAP.put("oda", "application/oda");
		MAP.put("odb", "application/vnd.oasis.opendocument.database");
		MAP.put("odc", "application/vnd.oasis.opendocument.chart");
		MAP.put("odf", "application/vnd.oasis.opendocument.formula");
		MAP.put("odft", "application/vnd.oasis.opendocument.formula-template");
		MAP.put("odg", "application/vnd.oasis.opendocument.graphics");
		MAP.put("odi", "application/vnd.oasis.opendocument.image");
		MAP.put("odm", "application/vnd.oasis.opendocument.text-master");
		MAP.put("odp", "application/vnd.oasis.opendocument.presentation");
		MAP.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
		MAP.put("odt", "application/vnd.oasis.opendocument.text");
		MAP.put("oga", "audio/ogg");
		MAP.put("ogg", "audio/ogg");
		MAP.put("ogv", "video/ogg");
		MAP.put("ogx", "application/ogg");
		MAP.put("omdoc", "application/omdoc+xml");
		MAP.put("onepkg", "application/onenote");
		MAP.put("onetmp", "application/onenote");
		MAP.put("onetoc", "application/onenote");
		MAP.put("onetoc2", "application/onenote");
		MAP.put("opf", "application/oebps-package+xml");
		MAP.put("opml", "text/x-opml");
		MAP.put("oprc", "application/vnd.palm");
		MAP.put("org", "application/vnd.lotus-organizer");
		MAP.put("osf", "application/vnd.yamaha.openscoreformat");
		MAP.put("osfpvg", "application/vnd.yamaha.openscoreformat.osfpvg+xml");
		MAP.put("otc", "application/vnd.oasis.opendocument.chart-template");
		MAP.put("otf", "font/otf");
		MAP.put("otg", "application/vnd.oasis.opendocument.graphics-template");
		MAP.put("oth", "application/vnd.oasis.opendocument.text-web");
		MAP.put("oti", "application/vnd.oasis.opendocument.image-template");
		MAP.put("otp", "application/vnd.oasis.opendocument.presentation-template");
		MAP.put("ots", "application/vnd.oasis.opendocument.spreadsheet-template");
		MAP.put("ott", "application/vnd.oasis.opendocument.text-template");
		MAP.put("oxps", "application/oxps");
		MAP.put("oxt", "application/vnd.openofficeorg.extension");
		MAP.put("p", "text/x-pascal");
		MAP.put("p10", "application/pkcs10");
		MAP.put("p12", "application/x-pkcs12");
		MAP.put("p7b", "application/x-pkcs7-certificates");
		MAP.put("p7c", "application/pkcs7-mime");
		MAP.put("p7m", "application/pkcs7-mime");
		MAP.put("p7r", "application/x-pkcs7-certreqresp");
		MAP.put("p7s", "application/pkcs7-signature");
		MAP.put("p8", "application/pkcs8");
		MAP.put("pas", "text/x-pascal");
		MAP.put("paw", "application/vnd.pawaafile");
		MAP.put("pbd", "application/vnd.powerbuilder6");
		MAP.put("pbm", "image/x-portable-bitmap");
		MAP.put("pcap", "application/vnd.tcpdump.pcap");
		MAP.put("pcf", "application/x-font-pcf");
		MAP.put("pcl", "application/vnd.hp-pcl");
		MAP.put("pclxl", "application/vnd.hp-pclxl");
		MAP.put("pct", "image/pict");
		MAP.put("pcurl", "application/vnd.curl.pcurl");
		MAP.put("pcx", "image/x-pcx");
		MAP.put("pdb", "application/vnd.palm");
		MAP.put("pdf", "application/pdf");
		MAP.put("pfa", "application/x-font-type1");
		MAP.put("pfb", "application/x-font-type1");
		MAP.put("pfm", "application/x-font-type1");
		MAP.put("pfr", "application/font-tdpfr");
		MAP.put("pfx", "application/x-pkcs12");
		MAP.put("pgm", "image/x-portable-graymap");
		MAP.put("pgn", "application/x-chess-pgn");
		MAP.put("pgp", "application/pgp-encrypted");
		MAP.put("pic", "image/pict");
		MAP.put("pict", "image/pict");
		MAP.put("pkg", "application/octet-stream");
		MAP.put("pki", "application/pkixcmp");
		MAP.put("pkipath", "application/pkix-pkipath");
		MAP.put("plb", "application/vnd.3gpp.pic-bw-large");
		MAP.put("plc", "application/vnd.mobius.plc");
		MAP.put("plf", "application/vnd.pocketlearn");
		MAP.put("pls", "audio/x-scpls");
		MAP.put("pml", "application/vnd.ctc-posml");
		MAP.put("png", "image/png");
		MAP.put("pnm", "image/x-portable-anymap");
		MAP.put("pnt", "image/x-macpaint");
		MAP.put("portpkg", "application/vnd.macports.portpkg");
		MAP.put("pot", "application/vnd.ms-powerpoint");
		MAP.put("potm", "application/vnd.ms-powerpoint.template.macroenabled.12");
		MAP.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
		MAP.put("ppam", "application/vnd.ms-powerpoint.addin.macroenabled.12");
		MAP.put("ppd", "application/vnd.cups-ppd");
		MAP.put("ppm", "image/x-portable-pixmap");
		MAP.put("pps", "application/vnd.ms-powerpoint");
		MAP.put("ppsm", "application/vnd.ms-powerpoint.slideshow.macroenabled.12");
		MAP.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
		MAP.put("ppt", "application/vnd.ms-powerpoint");
		MAP.put("pptm", "application/vnd.ms-powerpoint.presentation.macroenabled.12");
		MAP.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
		MAP.put("pqa", "application/vnd.palm");
		MAP.put("prc", "application/x-mobipocket-ebook");
		MAP.put("pre", "application/vnd.lotus-freelance");
		MAP.put("prf", "application/pics-rules");
		MAP.put("ps", "application/postscript");
		MAP.put("psb", "application/vnd.3gpp.pic-bw-small");
		MAP.put("psd", "image/vnd.adobe.photoshop");
		MAP.put("psf", "application/x-font-linux-psf");
		MAP.put("pskcxml", "application/pskc+xml");
		MAP.put("ptid", "application/vnd.pvi.ptid1");
		MAP.put("pub", "application/x-mspublisher");
		MAP.put("pvb", "application/vnd.3gpp.pic-bw-var");
		MAP.put("pwn", "application/vnd.3m.post-it-notes");
		MAP.put("pya", "audio/vnd.ms-playready.media.pya");
		MAP.put("pyv", "video/vnd.ms-playready.media.pyv");
		MAP.put("qam", "application/vnd.epson.quickanime");
		MAP.put("qbo", "application/vnd.intu.qbo");
		MAP.put("qfx", "application/vnd.intu.qfx");
		MAP.put("qps", "application/vnd.publishare-delta-tree");
		MAP.put("qt", "video/quicktime");
		MAP.put("qti", "image/x-quicktime");
		MAP.put("qtif", "image/x-quicktime");
		MAP.put("qwd", "application/vnd.quark.quarkxpress");
		MAP.put("qwt", "application/vnd.quark.quarkxpress");
		MAP.put("qxb", "application/vnd.quark.quarkxpress");
		MAP.put("qxd", "application/vnd.quark.quarkxpress");
		MAP.put("qxl", "application/vnd.quark.quarkxpress");
		MAP.put("qxt", "application/vnd.quark.quarkxpress");
		MAP.put("ra", "audio/x-pn-realaudio");
		MAP.put("ram", "audio/x-pn-realaudio");
		MAP.put("rar", "application/x-rar-compressed");
		MAP.put("ras", "image/x-cmu-raster");
		MAP.put("rcprofile", "application/vnd.ipunplugged.rcprofile");
		MAP.put("rdf", "application/rdf+xml");
		MAP.put("rdz", "application/vnd.data-vision.rdz");
		MAP.put("rep", "application/vnd.businessobjects");
		MAP.put("res", "application/x-dtbresource+xml");
		MAP.put("rgb", "image/x-rgb");
		MAP.put("rif", "application/reginfo+xml");
		MAP.put("rip", "audio/vnd.rip");
		MAP.put("ris", "application/x-research-info-systems");
		MAP.put("rl", "application/resource-lists+xml");
		MAP.put("rlc", "image/vnd.fujixerox.edmics-rlc");
		MAP.put("rld", "application/resource-lists-diff+xml");
		MAP.put("rm", "application/vnd.rn-realmedia");
		MAP.put("rmi", "audio/midi");
		MAP.put("rmp", "audio/x-pn-realaudio-plugin");
		MAP.put("rms", "application/vnd.jcp.javame.midlet-rms");
		MAP.put("rmvb", "application/vnd.rn-realmedia-vbr");
		MAP.put("rnc", "application/relax-ng-compact-syntax");
		MAP.put("roa", "application/rpki-roa");
		MAP.put("roff", "text/troff");
		MAP.put("rp9", "application/vnd.cloanto.rp9");
		MAP.put("rpss", "application/vnd.nokia.radio-presets");
		MAP.put("rpst", "application/vnd.nokia.radio-preset");
		MAP.put("rq", "application/sparql-query");
		MAP.put("rs", "application/rls-services+xml");
		MAP.put("rsd", "application/rsd+xml");
		MAP.put("rss", "application/rss+xml");
		MAP.put("rtf", "application/rtf");
		MAP.put("rtx", "text/richtext");
		MAP.put("s", "text/x-asm");
		MAP.put("s3m", "audio/s3m");
		MAP.put("saf", "application/vnd.yamaha.smaf-audio");
		MAP.put("sbml", "application/sbml+xml");
		MAP.put("sc", "application/vnd.ibm.secure-container");
		MAP.put("scd", "application/x-msschedule");
		MAP.put("scm", "application/vnd.lotus-screencam");
		MAP.put("scq", "application/scvp-cv-request");
		MAP.put("scs", "application/scvp-cv-response");
		MAP.put("scurl", "text/vnd.curl.scurl");
		MAP.put("sda", "application/vnd.stardivision.draw");
		MAP.put("sdc", "application/vnd.stardivision.calc");
		MAP.put("sdd", "application/vnd.stardivision.impress");
		MAP.put("sdkd", "application/vnd.solent.sdkm+xml");
		MAP.put("sdkm", "application/vnd.solent.sdkm+xml");
		MAP.put("sdp", "application/sdp");
		MAP.put("sdw", "application/vnd.stardivision.writer");
		MAP.put("see", "application/vnd.seemail");
		MAP.put("seed", "application/vnd.fdsn.seed");
		MAP.put("sema", "application/vnd.sema");
		MAP.put("semd", "application/vnd.semd");
		MAP.put("semf", "application/vnd.semf");
		MAP.put("ser", "application/java-serialized-object");
		MAP.put("setpay", "application/set-payment-initiation");
		MAP.put("setreg", "application/set-registration-initiation");
		MAP.put("sfd-hdstx", "application/vnd.hydrostatix.sof-data");
		MAP.put("sfs", "application/vnd.spotfire.sfs");
		MAP.put("sfv", "text/x-sfv");
		MAP.put("sgi", "image/sgi");
		MAP.put("sgl", "application/vnd.stardivision.writer-global");
		MAP.put("sgm", "text/sgml");
		MAP.put("sgml", "text/sgml");
		MAP.put("sh", "application/x-sh");
		MAP.put("shar", "application/x-shar");
		MAP.put("shf", "application/shf+xml");
		MAP.put("sid", "image/x-mrsid-image");
		MAP.put("sig", "application/pgp-signature");
		MAP.put("sil", "audio/silk");
		MAP.put("silo", "model/mesh");
		MAP.put("sis", "application/vnd.symbian.install");
		MAP.put("sisx", "application/vnd.symbian.install");
		MAP.put("sit", "application/x-stuffit");
		MAP.put("sitx", "application/x-stuffitx");
		MAP.put("skd", "application/vnd.koan");
		MAP.put("skm", "application/vnd.koan");
		MAP.put("skp", "application/vnd.koan");
		MAP.put("skt", "application/vnd.koan");
		MAP.put("sldm", "application/vnd.ms-powerpoint.slide.macroenabled.12");
		MAP.put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
		MAP.put("slt", "application/vnd.epson.salt");
		MAP.put("sm", "application/vnd.stepmania.stepchart");
		MAP.put("smf", "application/vnd.stardivision.math");
		MAP.put("smi", "application/smil+xml");
		MAP.put("smil", "application/smil+xml");
		MAP.put("smv", "video/x-smv");
		MAP.put("smzip", "application/vnd.stepmania.package");
		MAP.put("snd", "audio/basic");
		MAP.put("snf", "application/x-font-snf");
		MAP.put("so", "application/octet-stream");
		MAP.put("spc", "application/x-pkcs7-certificates");
		MAP.put("spf", "application/vnd.yamaha.smaf-phrase");
		MAP.put("spl", "application/x-futuresplash");
		MAP.put("spot", "text/vnd.in3d.spot");
		MAP.put("spp", "application/scvp-vp-response");
		MAP.put("spq", "application/scvp-vp-request");
		MAP.put("spx", "audio/ogg");
		MAP.put("sql", "application/x-sql");
		MAP.put("src", "application/x-wais-source");
		MAP.put("srt", "application/x-subrip");
		MAP.put("sru", "application/sru+xml");
		MAP.put("srx", "application/sparql-results+xml");
		MAP.put("ssdl", "application/ssdl+xml");
		MAP.put("sse", "application/vnd.kodak-descriptor");
		MAP.put("ssf", "application/vnd.epson.ssf");
		MAP.put("ssml", "application/ssml+xml");
		MAP.put("st", "application/vnd.sailingtracker.track");
		MAP.put("stc", "application/vnd.sun.xml.calc.template");
		MAP.put("std", "application/vnd.sun.xml.draw.template");
		MAP.put("stf", "application/vnd.wt.stf");
		MAP.put("sti", "application/vnd.sun.xml.impress.template");
		MAP.put("stk", "application/hyperstudio");
		MAP.put("stl", "application/vnd.ms-pki.stl");
		MAP.put("str", "application/vnd.pg.format");
		MAP.put("stw", "application/vnd.sun.xml.writer.template");
		MAP.put("sub", "text/vnd.dvb.subtitle");
		MAP.put("sus", "application/vnd.sus-calendar");
		MAP.put("susp", "application/vnd.sus-calendar");
		MAP.put("sv4cpio", "application/x-sv4cpio");
		MAP.put("sv4crc", "application/x-sv4crc");
		MAP.put("svc", "application/vnd.dvb.service");
		MAP.put("svd", "application/vnd.svd");
		MAP.put("svg", "image/svg+xml");
		MAP.put("svgz", "image/svg+xml");
		MAP.put("swa", "application/x-director");
		MAP.put("swf", "application/x-shockwave-flash");
		MAP.put("swi", "application/vnd.aristanetworks.swi");
		MAP.put("sxc", "application/vnd.sun.xml.calc");
		MAP.put("sxd", "application/vnd.sun.xml.draw");
		MAP.put("sxg", "application/vnd.sun.xml.writer.global");
		MAP.put("sxi", "application/vnd.sun.xml.impress");
		MAP.put("sxm", "application/vnd.sun.xml.math");
		MAP.put("sxw", "application/vnd.sun.xml.writer");
		MAP.put("t", "text/troff");
		MAP.put("t3", "application/x-t3vm-image");
		MAP.put("taglet", "application/vnd.mynfc");
		MAP.put("tao", "application/vnd.tao.intent-module-archive");
		MAP.put("tar", "application/x-tar");
		MAP.put("tcap", "application/vnd.3gpp2.tcap");
		MAP.put("tcl", "application/x-tcl");
		MAP.put("teacher", "application/vnd.smart.teacher");
		MAP.put("tei", "application/tei+xml");
		MAP.put("teicorpus", "application/tei+xml");
		MAP.put("tex", "application/x-tex");
		MAP.put("texi", "application/x-texinfo");
		MAP.put("texinfo", "application/x-texinfo");
		MAP.put("text", "text/plain");
		MAP.put("tfi", "application/thraud+xml");
		MAP.put("tfm", "application/x-tex-tfm");
		MAP.put("tga", "image/x-tga");
		MAP.put("thmx", "application/vnd.ms-officetheme");
		MAP.put("tif", "image/tiff");
		MAP.put("tiff", "image/tiff");
		MAP.put("tmo", "application/vnd.tmobile-livetv");
		MAP.put("torrent", "application/x-bittorrent");
		MAP.put("tpl", "application/vnd.groove-tool-template");
		MAP.put("tpt", "application/vnd.trid.tpt");
		MAP.put("tr", "text/troff");
		MAP.put("tra", "application/vnd.trueapp");
		MAP.put("trm", "application/x-msterminal");
		MAP.put("tsd", "application/timestamped-data");
		MAP.put("tsv", "text/tab-separated-values");
		MAP.put("ttc", "font/collection");
		MAP.put("ttf", "font/ttf");
		MAP.put("ttl", "text/turtle");
		MAP.put("twd", "application/vnd.simtech-mindmapper");
		MAP.put("twds", "application/vnd.simtech-mindmapper");
		MAP.put("txd", "application/vnd.genomatix.tuxedo");
		MAP.put("txf", "application/vnd.mobius.txf");
		MAP.put("txt", "text/plain");
		MAP.put("u32", "application/x-authorware-bin");
		MAP.put("udeb", "application/x-debian-package");
		MAP.put("ufd", "application/vnd.ufdl");
		MAP.put("ufdl", "application/vnd.ufdl");
		MAP.put("ulw", "audio/basic");
		MAP.put("ulx", "application/x-glulx");
		MAP.put("umj", "application/vnd.umajin");
		MAP.put("unityweb", "application/vnd.unity");
		MAP.put("uoml", "application/vnd.uoml+xml");
		MAP.put("uri", "text/uri-list");
		MAP.put("uris", "text/uri-list");
		MAP.put("urls", "text/uri-list");
		MAP.put("ustar", "application/x-ustar");
		MAP.put("utz", "application/vnd.uiq.theme");
		MAP.put("uu", "text/x-uuencode");
		MAP.put("uva", "audio/vnd.dece.audio");
		MAP.put("uvd", "application/vnd.dece.data");
		MAP.put("uvf", "application/vnd.dece.data");
		MAP.put("uvg", "image/vnd.dece.graphic");
		MAP.put("uvh", "video/vnd.dece.hd");
		MAP.put("uvi", "image/vnd.dece.graphic");
		MAP.put("uvm", "video/vnd.dece.mobile");
		MAP.put("uvp", "video/vnd.dece.pd");
		MAP.put("uvs", "video/vnd.dece.sd");
		MAP.put("uvt", "application/vnd.dece.ttml+xml");
		MAP.put("uvu", "video/vnd.uvvu.mp4");
		MAP.put("uvv", "video/vnd.dece.video");
		MAP.put("uvva", "audio/vnd.dece.audio");
		MAP.put("uvvd", "application/vnd.dece.data");
		MAP.put("uvvf", "application/vnd.dece.data");
		MAP.put("uvvg", "image/vnd.dece.graphic");
		MAP.put("uvvh", "video/vnd.dece.hd");
		MAP.put("uvvi", "image/vnd.dece.graphic");
		MAP.put("uvvm", "video/vnd.dece.mobile");
		MAP.put("uvvp", "video/vnd.dece.pd");
		MAP.put("uvvs", "video/vnd.dece.sd");
		MAP.put("uvvt", "application/vnd.dece.ttml+xml");
		MAP.put("uvvu", "video/vnd.uvvu.mp4");
		MAP.put("uvvv", "video/vnd.dece.video");
		MAP.put("uvvx", "application/vnd.dece.unspecified");
		MAP.put("uvvz", "application/vnd.dece.zip");
		MAP.put("uvx", "application/vnd.dece.unspecified");
		MAP.put("uvz", "application/vnd.dece.zip");
		MAP.put("vcard", "text/vcard");
		MAP.put("vcd", "application/x-cdlink");
		MAP.put("vcf", "text/x-vcard");
		MAP.put("vcg", "application/vnd.groove-vcard");
		MAP.put("vcs", "text/x-vcalendar");
		MAP.put("vcx", "application/vnd.vcx");
		MAP.put("vis", "application/vnd.visionary");
		MAP.put("viv", "video/vnd.vivo");
		MAP.put("vob", "video/x-ms-vob");
		MAP.put("vor", "application/vnd.stardivision.writer");
		MAP.put("vox", "application/x-authorware-bin");
		MAP.put("vrml", "model/vrml");
		MAP.put("vsd", "application/vnd.visio");
		MAP.put("vsf", "application/vnd.vsf");
		MAP.put("vss", "application/vnd.visio");
		MAP.put("vst", "application/vnd.visio");
		MAP.put("vsw", "application/vnd.visio");
		MAP.put("vtu", "model/vnd.vtu");
		MAP.put("vxml", "application/voicexml+xml");
		MAP.put("w3d", "application/x-director");
		MAP.put("wad", "application/x-doom");
		MAP.put("wasm", "application/wasm");
		MAP.put("wav", "audio/x-wav");
		MAP.put("wax", "audio/x-ms-wax");
		MAP.put("wbmp", "image/vnd.wap.wbmp");
		MAP.put("wbs", "application/vnd.criticaltools.wbs+xml");
		MAP.put("wbxml", "application/vnd.wap.wbxml");
		MAP.put("wcm", "application/vnd.ms-works");
		MAP.put("wdb", "application/vnd.ms-works");
		MAP.put("wdp", "image/vnd.ms-photo");
		MAP.put("weba", "audio/webm");
		MAP.put("webm", "video/webm");
		MAP.put("webp", "image/webp");
		MAP.put("wg", "application/vnd.pmi.widget");
		MAP.put("wgt", "application/widget");
		MAP.put("wks", "application/vnd.ms-works");
		MAP.put("wm", "video/x-ms-wm");
		MAP.put("wma", "audio/x-ms-wma");
		MAP.put("wmd", "application/x-ms-wmd");
		MAP.put("wmf", "application/x-msmetafile");
		MAP.put("wml", "text/vnd.wap.wml");
		MAP.put("wmlc", "application/vnd.wap.wmlc");
		MAP.put("wmls", "text/vnd.wap.wmlscript");
		MAP.put("wmlsc", "application/vnd.wap.wmlscriptc");
		MAP.put("wmv", "video/x-ms-wmv");
		MAP.put("wmx", "video/x-ms-wmx");
		MAP.put("wmz", "application/x-msmetafile");
		MAP.put("woff", "font/woff");
		MAP.put("woff2", "font/woff2");
		MAP.put("wpd", "application/vnd.wordperfect");
		MAP.put("wpl", "application/vnd.ms-wpl");
		MAP.put("wps", "application/vnd.ms-works");
		MAP.put("wqd", "application/vnd.wqd");
		MAP.put("wri", "application/x-mswrite");
		MAP.put("wrl", "model/vrml");
		MAP.put("wsdl", "application/wsdl+xml");
		MAP.put("wspolicy", "application/wspolicy+xml");
		MAP.put("wtb", "application/vnd.webturbo");
		MAP.put("wvx", "video/x-ms-wvx");
		MAP.put("x32", "application/x-authorware-bin");
		MAP.put("x3d", "model/x3d+xml");
		MAP.put("x3db", "model/x3d+binary");
		MAP.put("x3dbz", "model/x3d+binary");
		MAP.put("x3dv", "model/x3d+vrml");
		MAP.put("x3dvz", "model/x3d+vrml");
		MAP.put("x3dz", "model/x3d+xml");
		MAP.put("xaml", "application/xaml+xml");
		MAP.put("xap", "application/x-silverlight-app");
		MAP.put("xar", "application/vnd.xara");
		MAP.put("xbap", "application/x-ms-xbap");
		MAP.put("xbd", "application/vnd.fujixerox.docuworks.binder");
		MAP.put("xbm", "image/x-xbitmap");
		MAP.put("xdf", "application/xcap-diff+xml");
		MAP.put("xdm", "application/vnd.syncml.dm+xml");
		MAP.put("xdp", "application/vnd.adobe.xdp+xml");
		MAP.put("xdssc", "application/dssc+xml");
		MAP.put("xdw", "application/vnd.fujixerox.docuworks");
		MAP.put("xenc", "application/xenc+xml");
		MAP.put("xer", "application/patch-ops-error+xml");
		MAP.put("xfdf", "application/vnd.adobe.xfdf");
		MAP.put("xfdl", "application/vnd.xfdl");
		MAP.put("xht", "application/xhtml+xml");
		MAP.put("xhtml", "application/xhtml+xml");
		MAP.put("xhvml", "application/xv+xml");
		MAP.put("xif", "image/vnd.xiff");
		MAP.put("xla", "application/vnd.ms-excel");
		MAP.put("xlam", "application/vnd.ms-excel.addin.macroenabled.12");
		MAP.put("xlc", "application/vnd.ms-excel");
		MAP.put("xlf", "application/x-xliff+xml");
		MAP.put("xlm", "application/vnd.ms-excel");
		MAP.put("xls", "application/vnd.ms-excel");
		MAP.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroenabled.12");
		MAP.put("xlsm", "application/vnd.ms-excel.sheet.macroenabled.12");
		MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		MAP.put("xlt", "application/vnd.ms-excel");
		MAP.put("xltm", "application/vnd.ms-excel.template.macroenabled.12");
		MAP.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
		MAP.put("xlw", "application/vnd.ms-excel");
		MAP.put("xm", "audio/xm");
		MAP.put("xml", "application/xml");
		MAP.put("xo", "application/vnd.olpc-sugar");
		MAP.put("xop", "application/xop+xml");
		MAP.put("xpi", "application/x-xpinstall");
		MAP.put("xpl", "application/xproc+xml");
		MAP.put("xpm", "image/x-xpixmap");
		MAP.put("xpr", "application/vnd.is-xpr");
		MAP.put("xps", "application/vnd.ms-xpsdocument");
		MAP.put("xpw", "application/vnd.intercon.formnet");
		MAP.put("xpx", "application/vnd.intercon.formnet");
		MAP.put("xsl", "application/xml");
		MAP.put("xslt", "application/xslt+xml");
		MAP.put("xsm", "application/vnd.syncml+xml");
		MAP.put("xspf", "application/xspf+xml");
		MAP.put("xul", "application/vnd.mozilla.xul+xml");
		MAP.put("xvm", "application/xv+xml");
		MAP.put("xvml", "application/xv+xml");
		MAP.put("xwd", "image/x-xwindowdump");
		MAP.put("xyz", "chemical/x-xyz");
		MAP.put("xz", "application/x-xz");
		MAP.put("yang", "application/yang");
		MAP.put("yin", "application/yin+xml");
		MAP.put("z", "application/x-compress");
		MAP.put("z1", "application/x-zmachine");
		MAP.put("z2", "application/x-zmachine");
		MAP.put("z3", "application/x-zmachine");
		MAP.put("z4", "application/x-zmachine");
		MAP.put("z5", "application/x-zmachine");
		MAP.put("z6", "application/x-zmachine");
		MAP.put("z7", "application/x-zmachine");
		MAP.put("z8", "application/x-zmachine");
		MAP.put("zaz", "application/vnd.zzazz.deck+xml");
		MAP.put("zip", "application/zip");
		MAP.put("zir", "application/vnd.zul");
		MAP.put("zirz", "application/vnd.zul");
		MAP.put("zmm", "application/vnd.handheld-entertainment+xml");
	}
	private static Logger LOGGER = LoggerFactory.getLogger(FileOut.class);

	public static File getImageResizedFile(File image, String resize) {

		// 获取尺寸表达式，同时过滤非法的字符或路径
		Dimension size = FileOut.getImageResize(resize);
		if (size == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(image.getAbsolutePath());
		sb.append("$resized");
		sb.append(File.separator);
		sb.append(size.width);
		sb.append("x");
		sb.append(size.height);
		sb.append(".jpg");
		String imgSizePath = sb.toString();

		File imgSize = new File(imgSizePath);
		if (imgSize.exists()) {
			return imgSize;
		} else {
			return null;
		}
	}

	/**
	 * Parse the size string to the dimension, filter invalid char
	 * 
	 * @param resize 800x600
	 * @return null or dimension
	 */
	public static Dimension getImageResize(String resize) {
		if (StringUtils.isBlank(resize)) {
			return null;
		}

		String[] s1 = resize.toLowerCase().split("x");
		;
		if (s1.length != 2) {
			return null;
		}
		int w = 0;
		int h = 0;
		try {
			w = Integer.parseInt(s1[0]);
			h = Integer.parseInt(s1[1]);

			Dimension d = new Dimension();
			d.setSize(w, h);

			return d;
		} catch (Exception e) {
			LOGGER.error("ImageResize: {},{}", resize, e.getMessage());
			return null;
		}
	}

	private HttpServletRequest request;
	private HttpServletResponse response;
	private File file;
	private int httpStatusCode;

	public FileOut(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}

	public boolean initFile(String filePath) {
		if (filePath == null) {
			httpStatusCode = 404;
			response.setStatus(404);
			return false;
		}
		File file = new File(filePath);

		return this.initFile(file);

	}

	public boolean outBufferedImage(BufferedImage image) {
		if (image == null) {
			httpStatusCode = 404;
			response.setStatus(httpStatusCode);
			return false;
		}
		ServletOutputStream output;
		String imageFormat = "jpeg";
		try {
			this.outContetType(imageFormat);
			output = response.getOutputStream();
			ImageIO.write(image, imageFormat, output);
			httpStatusCode = 200;
			return true;
		} catch (IOException e) {
			httpStatusCode = 500;
			response.setStatus(httpStatusCode);
			return false;
		}

	}

	public boolean initFile(File file) {

		if (!file.exists()) {
			httpStatusCode = 404;
			response.setStatus(404);
			return false;
		}
		if (file.isDirectory()) {
			// Bad Request
			httpStatusCode = 400;
			response.setStatus(400);
			return false;
		}
		if (!file.canRead()) {
			// Forbidden
			httpStatusCode = 403;
			response.setStatus(403);
			return false;
		}
		httpStatusCode = 200;
		this.file = file;
		return true;

	}

	public void outContetType() {
		String ext = UFile.getFileExt(file.getName()).toLowerCase();
		this.outContetType(ext);
	}

	public void outContetType(String ext) {
		if (MAP.containsKey(ext)) {
			response.setContentType(MAP.get(ext));
		} else {
			response.setContentType(DEF_DOWNLOAD_TYPE);
		}
	}

	public void addCacheControl(Long cacheLife) {
		if (cacheLife != null) {
			response.setHeader("Cache-Control", "" + cacheLife);
			response.setHeader("Age", "" + cacheLife);
			response.setDateHeader("Expires", System.currentTimeMillis() + cacheLife * 1000);
		}
	}

	public boolean chcekIfNotModified() {
		String lastModified = Utils.getDateGMTString(new Date(file.lastModified()));
		if (lastModified.equals(request.getHeader("If-Modified-Since"))) {
			// 资源没有变化，返回 HTTP 304（Not Changed.）
			response.setStatus(304);
			return true;
		}
		response.setHeader("Last-Modified", lastModified);
		return false;
	}

	/**
	 * Download the file
	 * 
	 * @param downloadName
	 */

	public int download(String downloadName) {
		String name = file.getName();
		String ext = UFile.getFileExt(file.getName());
		if (ext.length() == 0) {
			ext = "bin";
			name = name + "." + ext;
		}
		if (!StringUtils.isBlank(downloadName)) {
			// \ / : * ? " < > |
			downloadName = downloadName.replace("/", "_").replace("\\", "_").replace("?", "_").replace("*", "_")
					.replace("|", "_").replace(":", "_").replace("<", "_").replace(">", "_").replace("\"", "_");

			String fileNoExit = UFile.getFileNoExt(downloadName);

			// Keep the file extension consistent
			name = fileNoExit + "." + ext;
		}
		name = Utils.textToUrl(name);
		name = name.replace("+", " ");

		response.setHeader("Location", name);
		response.setHeader("Cache-Control", "max-age=0");
		response.setHeader("Content-Disposition", "attachment; filename=" + name);

		//response.setContentType("image/oct");
		outContetType();

		return this.outFileBytesToClient();
	}

	/**
	 * Out file bytes in-line, e.g. image, pdf, and check the header flag If-Modified(304) and output the header
	 * Cache-control(OneWeek)
	 * 
	 * @return The length of the file
	 */
	public int outFileBytesInline() {
		long oneWeek = 604800L; // seconds
		return this.outFileBytesInline(true, oneWeek);
	}

	/**
	 * Out file bytes in-line, e.g. image, pdf
	 * 
	 * @param checkIfModified Whether to check the header If-Modified flag (304)
	 * @param cacheLife       Whether to output the Cache-Control header
	 * @return The length of the file
	 */
	public int outFileBytesInline(boolean checkIfModified, Long cacheLife) {

		// check If-Modified-Since 304
		if (checkIfModified && chcekIfNotModified()) {
			return -1;
		}

		// image type
		outContetType();
		// cache-control,age
		addCacheControl(cacheLife);

		// Out the file's bytes to user client
		return outFileBytesToClient();

	}

	public int outFileBytesInline(RestfulResult<Object> result, boolean checkIfModified, Long cacheLife) {
		if (httpStatusCode != 200) {
			result.setHttpStatusCode(httpStatusCode);
			result.setSuccess(false);
			return -1;
		}

		// 检查是否被修改 If-Modified-Since
		if (checkIfModified && chcekIfNotModified()) {
			result.setSuccess(true);
			result.setHttpStatusCode(304); // not modified
			return -1;
		}

		outContetType();
		addCacheControl(cacheLife);

		// Out the file's bytes to user client
		int length = this.outFileBytesToClient();
		if (length == -1) {
			result.setCode(500);
			result.setHttpStatusCode(500);
			result.setSuccess(false);
		} else {
			result.setCode(200);
			result.setHttpStatusCode(200);
			result.setSuccess(true);
		}
		return length;
	}

	/**
	 * Out the file's bytes to user client(response)
	 * 
	 * @param file     the file
	 * @param response HttpServletResponse
	 * @return out bytes length
	 */
	public int outFileBytesToClient() {
		FileInputStream input = null;
		try {
			input = new FileInputStream(file);
			return IOUtils.copy(input, response.getOutputStream());
		} catch (Exception err) {
			response.setStatus(500);
			LOGGER.error("out image: {}, {}", file.getAbsolutePath(), err.getMessage());
			return -1;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception err1) {
					LOGGER.error("out image, close input: {}, {}", file.getAbsolutePath(), err1.getMessage());
				}
			}
		}
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public File getFile() {
		return file;
	}

	public int getHttpStatusCode() {
		return httpStatusCode;
	}
}
