package org.emau.icmv.ganimed.ttp.cm2.frontend.beans;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2017 The MOSAIC Project - Institut fuer Community Medicine der
 * 							Universitaetsmedizin Greifswald - mosaic-projekt@uni-greifswald.de
 * 							concept and implementation
 * 							l. geidel
 * 							web client
 * 							g. weiher
 * 							a. blumentritt
 * 							please cite our publications
 * 							http://dx.doi.org/10.3414/ME14-01-0133
 * 							http://dx.doi.org/10.1186/s12967-015-0545-6
 * __
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import java.io.ByteArrayInputStream;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpSession;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean for displaying media(special getters are needed for correct display). Renders Scan-pdf and signature-png
 * 
 * @author weiherg
 * 
 */
@ManagedBean(name = "mediaProvider")
@RequestScoped
public class MediaProviderBean {

	private static final Logger logger = LoggerFactory.getLogger(MediaProviderBean.class);

	public DefaultStreamedContent getConsentPDFStreamDownload() {
		FacesContext context = FacesContext.getCurrentInstance();
		DefaultStreamedContent consentPDFStream;

		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		String scanBase64 = (String) session.getAttribute("preview_pdf");
		String name = (String) session.getAttribute("preview_name") + ".pdf";
		byte[] out = Base64.decode(scanBase64);
		consentPDFStream = new DefaultStreamedContent(new ByteArrayInputStream(out), "application/pdf", name);
		return consentPDFStream;
	}

	public StreamedContent getConsentPDFStream() {
		FacesContext context = FacesContext.getCurrentInstance();
		DefaultStreamedContent consentPDFStream;

		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
			// So, we're rendering the HTML. Return a stub StreamedContent so
			// that it will generate right URL.
			consentPDFStream = new DefaultStreamedContent();
		} else {
			HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
			String scanBase64 = (String) session.getAttribute("preview_pdf");
			String name = (String) session.getAttribute("preview_name") + ".pdf";
			byte[] out = Base64.decode(scanBase64);
			consentPDFStream = new DefaultStreamedContent(new ByteArrayInputStream(out), "application/pdf", name);
		}

		return consentPDFStream;
	}

	public StreamedContent getPatientSignatureStream() {
		FacesContext context = FacesContext.getCurrentInstance();
		DefaultStreamedContent stream = new DefaultStreamedContent();

		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
			// So, we're rendering the HTML. Return a stub StreamedContent so
			// that it will generate right URL.
		} else {
			HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
			String scanBase64 = (String) session.getAttribute("patient_signature");
			// String name = (String) session.getAttribute("patient_name")+".png";
			if (scanBase64 != null && !scanBase64.isEmpty()) {
				byte[] out = Base64.decode(scanBase64);
				if (out != null) {
					stream = new DefaultStreamedContent(new ByteArrayInputStream(out), "image/png", "patient_signature.png");
				} else {
					logger.warn("patient signature '" + scanBase64 + "'is not base64 encoded");
				}
			} else {
				if (logger.isInfoEnabled()) {
					logger.info("no patient signature scan found");
				}
			}
		}

		return stream;
	}

	public StreamedContent getPhysicianSignatureStream() {
		FacesContext context = FacesContext.getCurrentInstance();
		DefaultStreamedContent stream = new DefaultStreamedContent();

		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
			// So, we're rendering the HTML. Return a stub StreamedContent so
			// that it will generate right URL.
		} else {
			HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
			String scanBase64 = (String) session.getAttribute("physician_signature");
			// String name = (String) session.getAttribute("physician_name")+".png";
			if (scanBase64 != null && !scanBase64.isEmpty()) {
				byte[] out = Base64.decode(scanBase64);
				if (out != null) {
					stream = new DefaultStreamedContent(new ByteArrayInputStream(out), "image/png", "physician_signature.png");
				} else {
					logger.warn("physician signature '" + scanBase64 + "'is not base64 encoded");
				}
			} else {
				if (logger.isInfoEnabled()) {
					logger.info("no physician signature scan found");
				}
			}
		}

		return stream;
	}
}
