/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.server.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.engine.JRScriptletException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRExportProgressMonitor;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.type.OrientationEnum;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.HtmlExporterConfiguration;
import net.sf.jasperreports.export.HtmlReportConfiguration;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleCsvExporterConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleHtmlReportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.WriterExporterOutput;
import net.sf.jasperreports.export.type.HtmlSizeUnitEnum;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;
import net.sf.jasperreports.web.util.WebHtmlResourceHandler;

import com.avtoticket.server.db.ConnectionUtil;
import com.avtoticket.server.db.DB;
import com.avtoticket.server.utils.UserUtil;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.core.Report;
import com.avtoticket.shared.models.core.ReportParam;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.SerializationException;

/**
 * @author Minu <<a href='minu-moto@mail.ru'>minu-moto@mail.ru</a>>
 * @since 07.03.2012 9:35:32
 */
public class ReportServlet extends ExtHttpServlet {

	private static final long serialVersionUID = -4013293483275673877L;

	private static Logger logger = LoggerFactory.getLogger(ReportServlet.class);

	private static final long reportTimeout = logger.isDebugEnabled() ? 100 : 1000;

	private static final String REPORT_FOLDER = "reports/";
	private static File reportFolder;

	/**
	 * Название параметра отчёта, в котором передаётся путь к вложенным отчётам
	 */
	public static final String SUBREPORT_DIR = "SUBREPORT_DIR";

	/**
	 * Название параметра отчёта, в котором передаётся формат отчёта
	 * @see
	 * <ul>
	 * <li>{@link #REPORT_FORMAT_PDF}
	 * <li>{@link #REPORT_FORMAT_DOCX}
	 * <li>{@link #REPORT_FORMAT_RTF}
	 * <li>{@link #REPORT_FORMAT_XLS}
	 * <li>{@link #REPORT_FORMAT_XLSX}
	 * <li>{@link #REPORT_FORMAT_HTML}
	 * <li>{@link #REPORT_FORMAT_ODT}
	 * <li>{@link #REPORT_FORMAT_TXT}
	 * <li>{@link #REPORT_FORMAT_CSV}
	 * <ul>
	 */
	public static final String REPORT_FORMAT = "REPORT_FORMAT";

	/**
	 * Название параметра отчёта, в котором передаётся адрес генератора картинок
	 */
	public static final String IMG_URL = "IMG_URL";

	/**
	 * Название параметра отчёта, в котором передаётся корневой адрес приложения
	 */
	public static final String ROOT_CONTEXT = "ROOT_CONTEXT";

	/**
	 * Название параметра отчёта, в котором передаётся идентификатор текущего пользователя
	 */
	public static final String CURRENT_USER_ID = "CURRENT_USER_ID";

	/**
	 * Название параметра запроса, определяющего формат отчёта, получаемого с сервера
	 * <p>
	 * Допустимые значения параметра:
	 * <ul>
	 * <li>{@link #REPORT_FORMAT_PDF}
	 * <li>{@link #REPORT_FORMAT_DOCX}
	 * <li>{@link #REPORT_FORMAT_RTF}
	 * <li>{@link #REPORT_FORMAT_XLS}
	 * <li>{@link #REPORT_FORMAT_XLSX}
	 * <li>{@link #REPORT_FORMAT_HTML}
	 * <li>{@link #REPORT_FORMAT_ODT}
	 * <li>{@link #REPORT_FORMAT_TXT}
	 * <li>{@link #REPORT_FORMAT_CSV}
	 * <ul>
	 */
	public static final String REPORT_FORMAT_KEY = "format";

	/**
	 * Экспорт отчёта в формат Adobe Acrobat Document (*.pdf)
	 * @see #REPORT_FORMAT_KEY
	 */
	public static final String REPORT_FORMAT_PDF = "pdf";

	/**
	 * Экспорт отчёта в формат Microsoft Word Document (*.docx)
	 * @see #REPORT_FORMAT_KEY
	 */
	public static final String REPORT_FORMAT_DOCX = "docx";

	/**
	 * Экспорт отчёта в формат Rich Text Format (*.rtf)
	 * @see #REPORT_FORMAT_KEY
	 */
	public static final String REPORT_FORMAT_RTF = "rtf";

	/**
	 * Экспорт отчёта в формат Microsoft Excel 97-2003 Document (*.xls) (экспортер по умолчанию)
	 * @see #REPORT_FORMAT_KEY
	 */
	public static final String REPORT_FORMAT_XLS = "xls";

	/**
	 * Экспорт отчёта в формат Microsoft Excel Document (*.xlsx)
	 * @see #REPORT_FORMAT_KEY
	 */
	public static final String REPORT_FORMAT_XLSX = "xlsx";

	/**
	 * Экспорт отчёта в формат Веб-страница (*.html)
	 * @see #REPORT_FORMAT_KEY
	 */
	public static final String REPORT_FORMAT_HTML = "html";

	/**
	 * Экспорт отчёта в формат OpenDocument Text (*.odt)
	 * @see #REPORT_FORMAT_KEY
	 */
	public static final String REPORT_FORMAT_ODT = "odt";

	/**
	 * Экспорт отчёта в формат Text (*.txt)
	 * @see #REPORT_FORMAT_KEY
	 */
	public static final String REPORT_FORMAT_TXT = "txt";

	/**
	 * Экспорт отчёта в формат Comma-Separated Values (*.csv)
	 * @see #REPORT_FORMAT_KEY
	 */
	public static final String REPORT_FORMAT_CSV = "csv";

	public static void compileReport(Report repModel) throws Exception {
		try {
			if (!repModel.getReportForm().isEmpty() && !repModel.getSign().isEmpty()) {
				JasperDesign jd = JRXmlLoader.load(new ByteArrayInputStream(repModel.getReportForm().getBytes("UTF-8")));
				deleteReport(repModel);
				JasperCompileManager.compileReportToFile(jd,
						reportFolder.getPath() + File.separatorChar + repModel.getSign() + ".jasper");
			}
		} catch (Exception e) {
			logger.error("Возникла ошибка при компиляции отчёта", e);
			throw new SerializationException("Возникла ошибка при компиляции отчёта");
		}
	}

	public static void deleteReport(Report repModel) {
		try {
			Path path = FileSystems.getDefault().getPath(reportFolder.getPath(),
					repModel.getSign() + ".jasper");
			if (!repModel.getSign().isEmpty() && path.toFile().exists())
				Files.delete(path);
		} catch (Exception e) {
			logger.error("Возникла ошибка при удалении отчёта", e);
		}
	}

	public static void deleteReports(List<Long> ids) {
		try {
			List<Report> reports = DB.getModels(Report.class, ids);
			if (reports != null)
				reports.forEach(ReportServlet::deleteReport);
		} catch (Exception e) {
			logger.error("Возникла ошибка при удалении отчётов", e);
		}
	}

	public static Report getReportBySign(HttpServletRequest req, HttpServletResponse resp, String sign) throws SerializationException {
		try {
			Report ret = DB.getModel(Report.class, Where.equals(Report.SIGN, sign));
			if (ret == null)
				return null;
			JasperDesign jd = JRXmlLoader.load(new ByteArrayInputStream(ret.getReportForm().getBytes("UTF-8")));
			List<ReportParam> params = new ArrayList<ReportParam>();
			for (JRParameter param : jd.getParametersList())
				if (!param.isSystemDefined()) {
					ReportParam p = new ReportParam();
					p.setName(param.getName());
					p.setDescr(param.getDescription());
					p.setType(param.getValueClassName());
					p.setForPrompting(param.isForPrompting());
					if (param.getDefaultValueExpression() != null)
						p.setDefault(param.getDefaultValueExpression().getText());
					params.add(p);
				}
			if (!params.isEmpty())
				ret.setParams(params);
			JRPropertiesMap props = jd.getPropertiesMap();
			if ((props != null) && props.hasProperties()) {
				BaseModel prop = new BaseModel();
				for (String name : props.getPropertyNames())
					prop.set(name, props.getProperty(name));
				ret.setProps(prop);
			}
			return ret;
		} catch (Exception e) {
			logger.error("Возникла ошибка при получении данных", e);
			throw new SerializationException("Возникла ошибка при получении данных");
		}
	}

	private static void checkReports() {
		try {
			final List<File> reports = new ArrayList<File>();
			Files.walkFileTree(reportFolder.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.toString().endsWith(".jrxml"))
						reports.add(file.toFile());
					return super.visitFile(file, attrs);
				}
			});
			for (File rep : reports) {
				String fileName = rep.getName().substring(0, rep.getName().length() - 6).toLowerCase(Locale.getDefault());
				JasperDesign design = JRXmlLoader.load(rep);

				Report r = new Report(design.getName(), fileName,
						IOUtils.toString(new FileInputStream(rep), "UTF-8"));
				Report oldRep = DB.getModel(Report.class, Where.equals(Report.SIGN, r.getSign()));
				if ((oldRep == null) || !Objects.equals(oldRep.getName(), r.getName())
						 || !Objects.equals(oldRep.getSign(), r.getSign())
						 || !Objects.equals(oldRep.getReportForm(), r.getReportForm())) {
					if ((oldRep == null) || (oldRep.getId() == null))
						logger.info("Не найден отчёт '" + r.getName() + "'. Добавляем...");
					else {
						logger.info("Отчёт '" + r.getName() + "' изменился. Обновляем...");
						r.setId(oldRep.getId());
					}
					DB.save(r, -1L);
					compileReport(r);
				}
			}
		} catch (Exception e) {
			logger.error("Возникла ошибка при проверке отчётов", e);
		}
	}

	@Override
	public void init() throws ServletException {
		super.init();

		String root = UtilServlet.getRootPath() + "../";	// для отладки под jetty создаём отчёты в корне проекта
		if (root.indexOf("webapps") >= 0)					// для tomcat создаём отчёты в папке с самим сервером
			root += "../";
		reportFolder = new File(root + REPORT_FOLDER);
		if (!reportFolder.exists() && !reportFolder.mkdirs())
			logger.error("Произошла ошибка при инициализации движка отчётов: не удалось создать каталог '" + reportFolder.getAbsolutePath() + "'!");
		checkReports();

		DefaultJasperReportsContext.getInstance().setProperty("net.sf.jasperreports.text.save.line.breaks", "true");		// помогает от растягивания html-отчётов длинными строками без пробелов
	}

	private BaseModel parseParams(List<ReportParam> params, HttpServletRequest req, HttpServletResponse resp) {
		BaseModel ret = new BaseModel();
		if (params != null) {
			Map<String, String[]> pmap = req.getParameterMap();
			for (ReportParam param : params) {
				String val = null;
				if (SUBREPORT_DIR.equalsIgnoreCase(param.getName()))
					ret.put(SUBREPORT_DIR, reportFolder.getPath() + File.separatorChar);
				else if (REPORT_FORMAT.equalsIgnoreCase(param.getName()))
					ret.put(REPORT_FORMAT, req.getParameter(REPORT_FORMAT_KEY));
				else if (ROOT_CONTEXT.equalsIgnoreCase(param.getName()))
					ret.put(ROOT_CONTEXT, req.getScheme() + "://" + req.getServerName() + ((req.getServerPort() != 80) ? ":" + req.getServerPort() : "") + "/");
				else if (IMG_URL.equalsIgnoreCase(param.getName()))
					ret.put(IMG_URL, req.getScheme() + "://localhost" + ((req.getServerPort() != 80) ? ":" + req.getServerPort() : "") + "/download");
				else if (CURRENT_USER_ID.equalsIgnoreCase(param.getName())) {
					Long usrId = UserUtil.getUserIdFromSession(req);
					if (usrId == null) {
						response(resp, "Невозможно отобразить отчёт", "Не найден текущий пользователь");
						return null;
					}
					ret.put(CURRENT_USER_ID, usrId);
				} else {
					if (pmap.containsKey(param.getName())) {
						String[] v = pmap.get(param.getName());
						val = (v.length > 0) ? v[0] : null;
					} else
						val = param.getDefault();
					if ((val != null) && !val.isEmpty())
						if (Long.class.getName().equals(param.getType()))
							ret.put(param.getName(), Long.valueOf(val));
						else if (String.class.getName().equals(param.getType()))
							ret.put(param.getName(), val);
						else if (Boolean.class.getName().equals(param.getType()))
							ret.put(param.getName(), Boolean.valueOf(val));
						else if (Date.class.getName().equals(param.getType()))
							ret.put(param.getName(), /*SerializerUtil.parseDate(val)*/null);	// TODO
						else if (java.sql.Timestamp.class.getName().equals(param.getType())) {
							Date d = /*SerializerUtil.parseDate(val)*/null;		// TODO
							ret.put(param.getName(), (d != null) ? new java.sql.Timestamp(d.getTime()) : null);
						} else if (Collection.class.getName().equals(param.getType())) {
							ret.put(param.getName(), Lists.transform(Arrays.asList(val.split(",")), new Function<String, Long>() {
								@Override
								public Long apply(String input) {
									return ((input != null) && !input.isEmpty() && !"null".equalsIgnoreCase(input)) ? Long.valueOf(input) : null;
								}
							}));
						}
				}
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.reset();

		String format = req.getParameter(REPORT_FORMAT_KEY);
		if ((format == null) || format.isEmpty()) {
			responseHtml(resp, "Не задан формат отчета");
			return;
		}
		String sign = req.getParameter("sign");
		try {
			long time = new Date().getTime();

			Report rep = getReportBySign(req, resp, sign);
			if (rep == null) {
				responseHtml(resp, "Отчет с меткой " + sign + " не найден");
				return;
			}

			BaseModel params = parseParams(rep.getParams(), req, resp);
			if (params == null)
				return;
			BaseModel logParams = new BaseModel();
			logParams.fill(params);
			logParams.remove("sign");

			final JasperReport jr;
			try {
				jr = (JasperReport) JRLoader.loadObjectFromFile(reportFolder.getPath() + File.separatorChar + rep.getSign() + ".jasper");
			} catch (JRException e) {
				if ((e.getCause() != null) && (e.getCause() instanceof FileNotFoundException))
					throw new SerializationException("Отчёт '" + sign + "' не найден в системе");
				throw e;
			}

			long currTime = new Date().getTime();
			time = currTime - time;
			if (time >= reportTimeout)
				logger.warn("report " + sign + " load " + time + "ms params: " + logParams);
			time = currTime;

			JasperPrint jPrint = null;
			String jasperPrintSessionAttr = null;
			Connection con = ConnectionUtil.getConnection();
			try {
				jPrint = JasperFillManager.fillReport(jr, params, con);
				jasperPrintSessionAttr = req.getParameter(ImageServlet.JASPER_PRINT_REQUEST_PARAMETER);
				if (jasperPrintSessionAttr != null)
					req.getSession().setAttribute(jasperPrintSessionAttr, jPrint);
				else
					req.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jPrint);
			} finally {
				ConnectionUtil.releaseResources(null, null, con);
			}
			if (jPrint == null)
				throw new Exception("Возникла ошибка при формировании отчёта.");

			currTime = new Date().getTime();
			time = currTime - time;
			if (time >= reportTimeout)
				logger.warn("report " + sign + " filling " + time + "ms params: " + logParams);
			time = currTime;

			final StringBuilder fn = new StringBuilder(128);
			fn.append(rep.getName())
					.append(new SimpleDateFormat("_yyyy-MM-dd_HH-mm").format(new Date()));
			StringBuilder ff = new StringBuilder(16);
			ff.append(".").append(format);
			if (format.endsWith("2"))
				ff.deleteCharAt(ff.length() - 1);
			resp.setContentType("application/octet-stream");
			resp.setHeader("Pragma", "no-cache");
			resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
			resp.setHeader("Expires", "Sat, 26 Jul 1997 05:00:00 GMT");
			if (!REPORT_FORMAT_HTML.equalsIgnoreCase(format))
				resp.setHeader("Content-Disposition", "inline; " + getFileName(req, fn.toString() + ff.toString()));

			Exporter<ExporterInput, ?, ?, ? extends ExporterOutput> jrExp = null;
			switch (format.toLowerCase(Locale.getDefault())) {
			case REPORT_FORMAT_HTML:
				resp.setContentType("text/html");
				resp.setCharacterEncoding("UTF-8");
				HtmlExporterConfiguration confHTML = new HtmlExporterConfiguration() {
					@Override
					public Boolean isOverrideHints() {
						return false;
					}

					@Override
					public String getHtmlHeader() {
						StringBuilder ret = new StringBuilder();
						ret.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
						ret.append("<html mozNoMarginBoxes=''>\n");		// mozNoMarginBoxes - отрубает колонтитулы в firefox'е
						ret.append("<head>\n");
						ret.append("  <title>").append(fn.toString()).append("</title>\n");
						ret.append("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n");
						ret.append("  <style type=\"text/css\">\n");
						ret.append("    a {text-decoration: none}\n");
						ret.append("    body {\n");
						ret.append("        margin: 0px;\n");
						ret.append("    }\n");
						ret.append("    div.page-container {\n");
						ret.append("        margin: 14px 0px;\n");
						ret.append("        page-break-after: always;\n");
						ret.append("        page-break-inside: avoid;\n");
						ret.append("    }\n");
						ret.append("    div.page-container:last-child {\n");
						ret.append("        page-break-after: auto;\n");
						ret.append("    }\n");
						ret.append("    @page {\n");					// в зависимости от ориентации отчёта, задаём ориентацию страницы для печати
						ret.append("        size: ").append((jr.getOrientationValue() == OrientationEnum.LANDSCAPE) ? "landscape" : "portrait").append(";\n");
						ret.append("        margin: 0mm 1.5mm;\n");		// отступы справа и слева немного сжимают содержимое. Это помогает в Firefox от уползания содержимого на следующую страницу.
						ret.append("    }\n");
						ret.append("    @media print {\n");				// во время печати убираем отступы между страницами
						ret.append("		body {\n");
						ret.append("        	zoom: 92%;\n");			// zoom немного сжимает содержимое. Это помогает в Chrome от уползания содержимого на следующую страницу.
						ret.append("    	}\n");
						ret.append("		div.page-container {\n");
						ret.append("        	margin: 0px;\n");
						ret.append("    	}\n");
						ret.append("		td.page-edging {\n");
						ret.append("        	display: none;\n");
						ret.append("    	}\n");
						ret.append("    }\n");
						ret.append("  </style>\n");
						ret.append("</head>\n");
						ret.append("<body text=\"#000000\" link=\"#000000\" alink=\"#000000\" vlink=\"#000000\">\n");
						ret.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n");
						ret.append("<tr><td width=\"50%\" class='page-edging'>&nbsp;</td><td align=\"center\">\n");
						ret.append("<div class='page-container'>\n");
						return ret.toString();
					}

					@Override
					public String getBetweenPagesHtml() {
						return "</div><div class='page-container'>\n";
					}

					@Override
					public String getHtmlFooter() {
						StringBuilder ret = new StringBuilder();
						ret.append("</div></td><td width=\"50%\" class='page-edging'>&nbsp;</td></tr>\n");
						ret.append("</table>\n");
						ret.append("</body>\n");
						ret.append("</html>\n");
						return ret.toString();
					}

					@Override
					public Boolean isFlushOutput() {
						return true;
					}
				};
				HtmlReportConfiguration confHTMLRep = new SimpleHtmlReportConfiguration() {
					@Override
					public HtmlSizeUnitEnum getSizeUnit() {
						return HtmlSizeUnitEnum.POINT;
					}
				};
				HtmlExporter jrHtmlExporter = new HtmlExporter();
				jrHtmlExporter.setConfiguration(confHTML);
				jrHtmlExporter.setConfiguration(confHTMLRep);
				jrExp = jrHtmlExporter;
				break;
			case REPORT_FORMAT_PDF:
				resp.setContentType("application/pdf");
				JRPdfExporter jrPdfExp = new JRPdfExporter();
				SimplePdfExporterConfiguration confPDF = new SimplePdfExporterConfiguration();
				confPDF.setPdfJavaScript("this.print({bUI: true, bSilent: false, bShrinkToFit: true});");
				confPDF.setMetadataTitle(rep.getName());
				jrPdfExp.setConfiguration(confPDF);
				jrExp = jrPdfExp;
				break;
			case REPORT_FORMAT_XLS:
				jrExp = new JRXlsExporter();
				break;
			case REPORT_FORMAT_XLSX:
				jrExp = new JRXlsxExporter();
				break;
			case REPORT_FORMAT_CSV:
				JRCsvExporter jrCsvExp = new JRCsvExporter();
				SimpleCsvExporterConfiguration jrCsvConf = new SimpleCsvExporterConfiguration();
				jrCsvConf.setFieldDelimiter(";");
				jrCsvExp.setConfiguration(jrCsvConf);
				jrExp = jrCsvExp;
				break;
			case REPORT_FORMAT_ODT:
				jrExp = new JROdtExporter();
				break;
			case REPORT_FORMAT_DOCX:
				jrExp = new JRDocxExporter();
				break;
			case REPORT_FORMAT_RTF:
				jrExp = new JRRtfExporter();
				break;
			case REPORT_FORMAT_TXT:
				jrExp = new JRTextExporter() {
					private char[][] createPage() {
						try {
							Field field = getClass().getSuperclass().getDeclaredField("pageData");
							field.setAccessible(true);
							field.set(this, new char[pageHeightInChars][]);
							return (char[][]) field.get(this);
						} catch (Exception e) {
							return null;
						}
					}

					@Override
					protected void exportPage(JRPrintPage jrPage) throws IOException {
						List<JRPrintElement> elements = jrPage.getElements();

						char[][] page = createPage();
						if (page == null)
							return;
						for (int i = 0; i < pageHeightInChars; i++) {
							page[i] = new char[pageWidthInChars];
							Arrays.fill(page[i], ' ');
						}

						exportElements(elements);

						for (int i = 0; i < pageHeightInChars; i++) {
							writer.write(new String(page[i]).replaceAll("\\s+$", ""));	// right trim
							writer.write(lineSeparator);
						}

						JRExportProgressMonitor progressMonitor = getCurrentItemConfiguration().getProgressMonitor();
						if (progressMonitor != null)
							progressMonitor.afterPageExport();
					}
				};
				break;
			default:
				responseHtml(resp, "Неопознанный формат отчета");
				break;
			}

			if (jrExp != null)
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
					jrExp.setExporterInput(new SimpleExporterInput(jPrint));
					switch (format.toLowerCase(Locale.getDefault())) {
					case REPORT_FORMAT_HTML:
						SimpleHtmlExporterOutput outp = new SimpleHtmlExporterOutput(baos);
						outp.setImageHandler(new WebHtmlResourceHandler("/repimgs?"
								+ ((jasperPrintSessionAttr != null) ? ImageServlet.JASPER_PRINT_REQUEST_PARAMETER + "=" + jasperPrintSessionAttr + "&" : "")
								+ ImageServlet.IMAGE_NAME_REQUEST_PARAMETER + "={0}&nocache=" + System.currentTimeMillis()));
						((Exporter<?, ?, ?, WriterExporterOutput>) jrExp).setExporterOutput(outp);
						break;
					case REPORT_FORMAT_RTF:
						((Exporter<?, ?, ?, WriterExporterOutput>) jrExp).setExporterOutput(new SimpleWriterExporterOutput(baos));
						break;
					case REPORT_FORMAT_CSV:
					case REPORT_FORMAT_TXT:
						((Exporter<?, ?, ?, WriterExporterOutput>) jrExp).setExporterOutput(new SimpleWriterExporterOutput(baos, "cp1251"));
						break;
					default:
						((Exporter<?, ?, ?, OutputStreamExporterOutput>) jrExp).setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
					}
					jrExp.exportReport();
					resp.setContentLength(baos.size());
					try {
						baos.writeTo(resp.getOutputStream());
					} catch (IOException e) {
						// отрабатываем обрыв соединения
						logger.warn(e.getClass() + ": " + e.getMessage());
						return;
					}
				}

			try {
				resp.flushBuffer();
			} catch (IOException e) {
				logger.warn(e.getClass() + ": " + e.getMessage());
			}

			currTime = new Date().getTime();
			time = currTime - time;
			if (time >= reportTimeout)
				logger.warn("report " + sign + " export " + time + "ms params: " + logParams);
		} catch (JRScriptletException e) {
			response(resp, "Ошибка", e.getMessage());
		} catch (Exception e) {
			logger.error("", e);
			response(resp, "Ошибка на стороне сервера", e.getMessage());
		}
	}

}