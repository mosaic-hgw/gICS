package org.emau.icmvc.ganimed.ttp.cm2.frontend.model;

import java.util.List;
import java.util.Map;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractGICSBean;

@ViewScoped
@Named("embeddedController")
public class EmbeddedController extends AbstractGICSBean
{
	private String title;
	private String help;
	private boolean useDomainLogo;
	private boolean prefilledTemplate;
	private boolean prefilledSignerId;
	private boolean digitalSignature;
	private boolean scanUpload;
	private boolean editable;
	private String redirect;
	private boolean redirectInIFrame;
	private boolean printButton;
	private boolean pdfDownloadButton;
	private boolean done;

	public void init()
	{
		// skip ajax posts
		if (FacesContext.getCurrentInstance().isPostback())
		{
			return;
		}
		
		prefilledTemplate = false;
		prefilledSignerId = false;
		done = false;

		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, String[]> parameters = ((HttpServletRequest) externalContext.getRequest()).getParameterMap();

		setTitle(getParameter(parameters, "title"));
		setHelp(getParameter(parameters, "help"));
		setUseDomainLogo(Boolean.parseBoolean(getParameter(parameters, "useDomainLogo")));
		setDigitalSignature(Boolean.parseBoolean(getParameter(parameters, "digitalSignature") == null ? "true" : getParameter(parameters, "digitalSignature")));
		setScanUpload(Boolean.parseBoolean(getParameter(parameters, "scanUpload")));
		setEditable(Boolean.parseBoolean(getParameter(parameters, "editable") == null ? "true" : getParameter(parameters, "editable")));
		setRedirect(getParameter(parameters, "redirect"));
		setRedirectInIFrame(Boolean.parseBoolean(getParameter(parameters, "redirectInIFrame")));
		setPrintButton(Boolean.parseBoolean(getParameter(parameters, "printButton")));
		setPdfDownloadButton(Boolean.parseBoolean(getParameter(parameters, "pdfDownloadButton")));
	}

	public String getTitle(String templateType)
	{
		if (StringUtils.isNotEmpty(title))
		{
			return title;
		}
		else if (StringUtils.isNotEmpty(templateType))
		{
			return getBundle().getString("page.templates.fill." + templateType);
		}
		else
		{
			return getBundle().getString("page.templates.fill");
		}
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getHelp()
	{
		return StringUtils.isEmpty(help) ? getBundle().getString("page.consents.prefill.help") : help;
	}

	public void setHelp(String help)
	{
		this.help = help;
	}

	public boolean isUseDomainLogo()
	{
		return useDomainLogo;
	}

	public void setUseDomainLogo(boolean useDomainLogo)
	{
		this.useDomainLogo = useDomainLogo;
	}

	public boolean isPrefilledTemplate()
	{
		return prefilledTemplate;
	}

	public void setPrefilledTemplate(boolean prefilledTemplate)
	{
		this.prefilledTemplate = prefilledTemplate;
	}

	public boolean isPrefilledSignerId()
	{
		return prefilledSignerId;
	}

	public void setPrefilledSignerId(boolean prefilledSignerId)
	{
		this.prefilledSignerId = prefilledSignerId;
	}

	public boolean isDigitalSignature()
	{
		return digitalSignature;
	}

	public void setDigitalSignature(boolean digitalSignature)
	{
		this.digitalSignature = digitalSignature;
	}

	public boolean isScanUpload()
	{
		return scanUpload;
	}

	public void setScanUpload(boolean scanUpload)
	{
		this.scanUpload = scanUpload;
	}

	public boolean isEditable()
	{
		return editable;
	}

	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}

	public String getRedirect()
	{
		return redirect;
	}

	public String getRedirectWithSignerIds(List<SignerIdDTO> signerIds)
	{
		StringBuilder sb = new StringBuilder(redirect);
		int i = 0;
		for (SignerIdDTO signerId : signerIds)
		{
			sb.append(i == 0 ? "?" : "&");
			sb.append(signerId.getIdType());
			sb.append("=");
			sb.append(signerId.getId());
			i++;
		}
		return sb.toString();
	}

	public void setRedirect(String redirect)
	{
		this.redirect = redirect;
	}

	public boolean isRedirectInIFrame()
	{
		return redirectInIFrame;
	}

	public void setRedirectInIFrame(boolean redirectInIFrame)
	{
		this.redirectInIFrame = redirectInIFrame;
	}

	public boolean isPrintButton()
	{
		return printButton;
	}

	public void setPrintButton(boolean printButton)
	{
		this.printButton = printButton;
	}

	public boolean isPdfDownloadButton()
	{
		return pdfDownloadButton;
	}

	public void setPdfDownloadButton(boolean pdfDownloadButton)
	{
		this.pdfDownloadButton = pdfDownloadButton;
	}

	public boolean isDone()
	{
		return done;
	}

	public void setDone(boolean done)
	{
		this.done = done;
	}
}
