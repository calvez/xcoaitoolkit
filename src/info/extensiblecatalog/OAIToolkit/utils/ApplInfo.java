/**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the  
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/. 
  *
  */

package info.extensiblecatalog.OAIToolkit.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import info.extensiblecatalog.OAIToolkit.DTOs.DataTransferObject;
import info.extensiblecatalog.OAIToolkit.DTOs.SetDTO;
import info.extensiblecatalog.OAIToolkit.configuration.OAIConfiguration;
import info.extensiblecatalog.OAIToolkit.db.DButil;
import info.extensiblecatalog.OAIToolkit.db.LuceneSearcher;
import info.extensiblecatalog.OAIToolkit.db.managers.SetsMgr;
import info.extensiblecatalog.OAIToolkit.oai.MetadataFormatUnmarshaler;
import info.extensiblecatalog.OAIToolkit.oai.MetadataFormats;
import info.extensiblecatalog.OAIToolkit.oai.StorageTypes;

/**
 * Basic informations about the application (directories, properties etc.)
 * @author Péter Király
 */
public class ApplInfo {
	
        private static String programmer_log = "programmer";
        private static final Logger prglog = Logging.getLogger(programmer_log);
    
	//private static final Logger logger = Logging.getLogger();
        	
	/** The OAI configuration file. Contains the configuration values
	 * for serving the OAI requests. See {@link #oaiConf}, 
	 * {@link OAIConfiguration} */
	private static final String OAI_SERVER_CNF_FILE = 
										"OAIToolkit.server.properties";
	
	/** Configuration file for logging */
	private static final String OAI_LOG4J_CNF_FILE = 
										"OAIToolkit.log4j.properties";
	
	/** Configuration file for database */
	private static final String OAI_DB_CNF_FILE = 
											"OAIToolkit.db.properties";
	
	/** Configuration file for directory */
	private static final String OAI_DIRECTORY_CNF_FILE = 
									"OAIToolkit.directory.properties";

	/** The log directory's name */
	private static String logDir;
	
	/** The directory, where the (mainly xsl) files are stored outside of
	 * the web application */
	private static String resourceDir;
	
	/** The directory, where the (mainly xsl) files are stored outside of
	 * the web application */
	public static LuceneSearcher luceneSearcher;
	
	/** The OAI configuration object. This object handles the
	 * storing and loading configuration values from the 
	 * {@link #oaiConfigFileName} */
	public static OAIConfiguration oaiConf;
	
	/** The object storing and loading the metadata formats used
	 * by the ListMetadataformats verb and metadataPrefix parameter */
	public static MetadataFormats metadataFormats;
	
	/** The Tomcat's bin directory. This is the place of some
	 * config files. */
	public static File tomcatBinDir;
	
	/** A hash table for caching set names by its id */
	public static final Map<Integer, String> setNamesById = 
		new HashMap<Integer, String>();

	/** A hash table for caching set ids by its name */
	public static final Map<String, Integer> setIdsByName = 
		new HashMap<String, Integer>();
	
	/** The line separator */
	public static final String LN = System.getProperty("line.separator");
	
	/** The cache directory */
	public static File cacheDirectory;
	
	/** List of error messages */
	// TODO: write out error messages in the index page
	public static final List<String> errorMessages = new ArrayList<String>();
	
	/** possible state of a cached request */
	public enum RequestState{ STARTED, FINISHED };

	/** state of the cached request */
	public static Map<String, RequestState> cacheRegister 
			= new HashMap<String, RequestState>();
	
	/** the timestamp of the last cache clearence 
	 * -1 means that there haven't been cache clearence yet */
	public static long lastCacheClear = -1;
	
	/** The minimum interval between two cache clearence */
	public static long cacheClearInterval = 5 * 60 * 1000;

	/** initialize the command line interface */
	public static void init(String logDir) throws Exception {
		//initApplication();
		System.out.println("init(logDir): " + logDir);
		initLogging(logDir);
		initDB(logDir);
		initSets();
	}

	public static void init(String rootDir, String logDir) throws Exception {
		//initApplication();
		initLogging(rootDir, logDir);
		initDB(rootDir);
		initSets();
	}

	/** initialize the OAI server */
	public static void init(File binDir) throws Exception {
		System.out.println("ApplInfo::init(" + binDir + ")");
		tomcatBinDir = binDir;
		initApplication();
		initLogging(tomcatBinDir.getAbsolutePath(), 
				logDir, OAI_LOG4J_CNF_FILE);
		//initLogging(logDir, new File(
		//		tomcatBinDir, OAI_LOG4J_CNF_FILE).getAbsolutePath());
		initDB();
		initSets();
	}

	/**
	 * Initialize the database setup
	 * @throws Exception
	 */
	public static void initDB() throws Exception {
		if(null != tomcatBinDir) {
			DButil.init(new File(tomcatBinDir, OAI_DB_CNF_FILE).getAbsolutePath());
		} else {
			DButil.init(OAI_DB_CNF_FILE);
		}
	}

	public static void initDB(String dir) throws Exception {
		DButil.init(new File(dir, OAI_DB_CNF_FILE).getAbsolutePath());
	}

	/**
	 * start logging
	 */
	public static void initLogging(String logDir) throws FileNotFoundException {
		initLogging(logDir, logDir);
	}

	/**
	 * start logging
	 */
	public static void initLogging(String rootDir, String logDir) throws FileNotFoundException {
		initLogging(rootDir, logDir, OAI_LOG4J_CNF_FILE);
	}

	/**
	 * start logging
	 */
	public static void initLogging(String rootDir, String logDir, 
			String propertyFileName) 
			throws FileNotFoundException {
		ClassLoader cloader = ApplInfo.class.getClassLoader();
		InputStream logProps = cloader.getResourceAsStream(propertyFileName);
		if(null == logProps) {
			prglog.info("[PRG] log4j property file is null");
			File propertyFile = new File(rootDir, propertyFileName);
			if(propertyFile.exists()) {
				try {
					prglog.info("[PRG] log4j property file: " 
							+ propertyFile.getAbsoluteFile());
					logProps = new FileInputStream(propertyFile);
				} catch(FileNotFoundException e) {
					prglog.error("[PRG] " + e);
					throw e;
				}
			}
		}
		if(null != logProps) {
			LoggingParameters logParams = new LoggingParameters(logDir);
			Logging.initLogging(logParams, logProps);
		}
	}
	
	/**
	 * Initialize the application. Setup {@link #logDir}, {@link #clone(),
	 * loading {@link #oaiConf} and {@link #metadataFormats}
	 * @throws Exception
	 */
	public static void initApplication() throws Exception {
		try {
			// aplication properties
			System.out.println("reading application properties");
			String basePropertiesFileName = OAI_DIRECTORY_CNF_FILE;
			File oaiConfigFile = null;
			if(null != tomcatBinDir) {
				basePropertiesFileName = new File(tomcatBinDir, 
						basePropertiesFileName).getAbsolutePath();
				oaiConfigFile = new File(tomcatBinDir, OAI_SERVER_CNF_FILE);
			}

			System.out.println("basePropertiesFileName: " + basePropertiesFileName);
			System.out.println("oaiConfigFile: " + oaiConfigFile);
			PropertiesConfiguration applConf = ConfigUtil
					.load(basePropertiesFileName);
			
			logDir = applConf.getString("logDir");
			System.out.println("ApplInfo::logDir: " + logDir);
			resourceDir = applConf.getString("resourceDir");
			System.out.println("ApplInfo::resourceDir: " + resourceDir);
			String luceneDir = applConf.getString("luceneDir");
			System.out.println("ApplInfo::luceneDir: " + luceneDir);
			String cacheDir = applConf.getString("cacheDir");
			System.out.println("ApplInfo::cacheDir: " + cacheDir);
			if(cacheDir == null) {
				errorMessages.add("There is no 'cacheDir' defined in " 
						+ basePropertiesFileName);
			} else {
				File cache = new File(cacheDir);
				if(!cache.exists()){
					errorMessages.add("The cache directory ("+ cacheDir 
							+ ") does not exist. The tool will create it.");
					boolean created = cache.mkdir();
				} else {
					if(cache.isDirectory()) {
						cacheDirectory = cache;
					} else {
						errorMessages.add("The cache directory ("+ cacheDir 
								+ ") is not directory.");
					}
				}
			}
			
			// oai server configuration
			prglog.trace("[PRG] reading OAI properties");
			if(null != oaiConfigFile) {
				prglog.info("[PRG] OAIConfiguration with oaiConfigFile: " 
						+ oaiConfigFile);
				oaiConf = new OAIConfiguration(oaiConfigFile);
			} else {
				prglog.info("[PRG] OAIConfiguration with oaiConfigFileName: " 
						+ OAI_SERVER_CNF_FILE);
				oaiConf = new OAIConfiguration(OAI_SERVER_CNF_FILE);
			}
			oaiConf.load();
			
			if(!oaiConf.getStorageType().equals(StorageTypes.MYSQL)) {
				if(luceneDir == null) {
					System.out.println("Lucene directory (luceneDir in OAIToolkit.directory.properties)" +
							" isn't set. Please set this directory!");
				} else {
					if(new File(luceneDir).exists()){
						luceneSearcher = new LuceneSearcher(luceneDir);
					} else {
						File f = new File(luceneDir);
						while(!f.exists()) {
							System.out.println("LuceneDir doesn't exist");
							f = f.getParentFile();
						}
						System.out.println(f.getAbsolutePath() + " exists");
					}
				}
			}

			// load metadata formats
			prglog.trace("[PRG] reading metadata formats");
			metadataFormats = MetadataFormatUnmarshaler.load(
					new File(resourceDir, "metadataFormats.xml"), 
					new File(resourceDir, "metadata-format-mapping.xml"));
			

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void initSets() throws Exception {
		SetsMgr mgr = new SetsMgr();
		List<DataTransferObject> all = mgr.get(new SetDTO());
		for(DataTransferObject item : all) {
			SetDTO set = (SetDTO)item;
			setNamesById.put(set.getSetId(), set.getSetSpec());
			setIdsByName.put(set.getSetSpec(), set.getSetId());
		}
	}

	public static String getLogDir() {
		return logDir;
	}

	public static void setLogDir(String logDir) {
		ApplInfo.logDir = logDir;
	}

	public static String getResourceDir() {
		return resourceDir;
	}

	public static void setResourceDir(String resourceDir) {
		ApplInfo.resourceDir = resourceDir;
	}
}