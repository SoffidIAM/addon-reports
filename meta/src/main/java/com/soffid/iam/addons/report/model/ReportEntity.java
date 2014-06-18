package com.soffid.iam.addons.report.model;

import java.util.Collection;

import com.soffid.iam.addons.report.api.Report;
import com.soffid.mda.annotation.Column;
import com.soffid.mda.annotation.DaoFinder;
import com.soffid.mda.annotation.Depends;
import com.soffid.mda.annotation.Entity;
import com.soffid.mda.annotation.ForeignKey;
import com.soffid.mda.annotation.Identifier;
import com.soffid.mda.annotation.Index;

import es.caib.seycon.ng.model.GrupEntity;
import es.caib.seycon.ng.model.RolEntity;
import es.caib.seycon.ng.model.UsuariEntity;

@Entity(table="SCR_REPORT")
@Depends({
	Report.class,
	UsuariEntity.class, RolEntity.class, GrupEntity.class
})
public class ReportEntity {
	@Identifier
	@Column(name="REP_ID")
	Long id;
	
	@Column(name="REP_NAME")
	String name;
	
	@Column(name="REP_DOCID", length=128)
	String docId;
	
	@ForeignKey(foreignColumn="RAC_REP_ID")
	Collection<ReportACLEntity> acl;

	@ForeignKey(foreignColumn="PAR_REP_ID")
	Collection<ReportParameterEntity> parameters;
	
	//////////            Finders
	
	@DaoFinder("select re from com.soffid.iam.addons.report.model.ReportEntity as re where re.name like :name")
	Collection<ReportEntity> findByNameFilter (String name ) {
		return null;
	}

	@DaoFinder
	ReportEntity findByName (String name ) {
		return null;
	}
}

@Index(columns={"REP_NAME"}, entity=ReportEntity.class, name="SCR_REPORT_NAME_NDX", unique=true)
class ReportEntityNameIndex
{
	
}