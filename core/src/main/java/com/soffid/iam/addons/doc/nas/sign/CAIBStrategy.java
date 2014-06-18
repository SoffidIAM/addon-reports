package com.soffid.iam.addons.doc.nas.sign;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import org.apache.log4j.Logger;

import com.soffid.iam.addons.doc.exception.NASException;
import com.soffid.iam.addons.doc.nas.NASManager;
import com.soffid.iam.addons.doc.nas.SignerStrategy;
import com.soffid.iam.addons.doc.model.DocumentEntity;

import es.caib.signatura.api.Signature;
import es.caib.signatura.api.Signer;
import es.caib.signatura.api.SignerFactory;

public class CAIBStrategy implements SignerStrategy 
{
	Logger log = Logger.getLogger(CAIBStrategy.class);
	/**
	 * @see com.soffid.iam.addons.doc.nas.SignerStrategy#validateSign(es.caib.bpm.nas.entity.DocumentEntity, byte[])
	 */
	public Signature validateSign(DocumentEntity document, Signature sign) throws NASException 
	{
		
		File documentFile= null;
		ByteArrayInputStream streamBytes= null;
		ObjectInputStream streamObjeto= null;
		boolean valido= false;
		
		try
		{
			documentFile= NASManager.getInstance().retreiveFile(document.getFsPath());		
			
			if (! sign.verify() )
			{
				log.warn("Certificate not valid for signature: "+sign.getCertSubjectCommonName());
				return null;
			}
			if ( ! sign.verifyAPosterioriTimestamp(new FileInputStream(documentFile)))
			{
				log.warn("Signature tampering for: "+sign.getCertSubjectCommonName());
				return null;
			}
		}
		catch(Exception ex)
		{
			throw new NASException(ex);
		}
		finally
		{
			NASManager.getInstance().cleanTemporaryResources();

			try
			{
				if(streamBytes!= null)
				{
					streamBytes.close();
				}

				if(streamObjeto!= null)
				{
					streamObjeto.close();
				}
			}
			catch(Exception ex)
			{
				throw new NASException(ex);
			}
		}
		
		return sign;
	}

}
