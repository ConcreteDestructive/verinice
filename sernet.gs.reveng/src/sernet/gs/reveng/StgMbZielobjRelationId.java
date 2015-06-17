package sernet.gs.reveng;

// Generated Jun 5, 2015 1:28:30 PM by Hibernate Tools 3.4.0.CR1

import java.util.Date;

/**
 * StgMbZielobjRelationId generated by hbm2java
 */
public class StgMbZielobjRelationId implements java.io.Serializable {

	private Integer zotId1;
	private Integer zotId2;
	private Integer zotImpId;
	private Character typ;
	private Character kardinalitaet;
	private Byte metaNeu;
	private Integer metaVers;
	private Integer obsoletVers;
	private String guid;
	private Date timestamp;
	private Date loeschDatum;
	private Byte impNeu;
	private String guidOrg;
	private Integer usn;
	private String erstelltDurch;
	private String geloeschtDurch;

	public StgMbZielobjRelationId() {
	}

	public StgMbZielobjRelationId(Integer zotId1, Integer zotId2,
			Integer zotImpId, Character typ, Character kardinalitaet,
			Byte metaNeu, Integer metaVers, Integer obsoletVers, String guid,
			Date timestamp, Date loeschDatum, Byte impNeu, String guidOrg,
			Integer usn, String erstelltDurch, String geloeschtDurch) {
		this.zotId1 = zotId1;
		this.zotId2 = zotId2;
		this.zotImpId = zotImpId;
		this.typ = typ;
		this.kardinalitaet = kardinalitaet;
		this.metaNeu = metaNeu;
		this.metaVers = metaVers;
		this.obsoletVers = obsoletVers;
		this.guid = guid;
		this.timestamp = timestamp;
		this.loeschDatum = loeschDatum;
		this.impNeu = impNeu;
		this.guidOrg = guidOrg;
		this.usn = usn;
		this.erstelltDurch = erstelltDurch;
		this.geloeschtDurch = geloeschtDurch;
	}

	public Integer getZotId1() {
		return this.zotId1;
	}

	public void setZotId1(Integer zotId1) {
		this.zotId1 = zotId1;
	}

	public Integer getZotId2() {
		return this.zotId2;
	}

	public void setZotId2(Integer zotId2) {
		this.zotId2 = zotId2;
	}

	public Integer getZotImpId() {
		return this.zotImpId;
	}

	public void setZotImpId(Integer zotImpId) {
		this.zotImpId = zotImpId;
	}

	public Character getTyp() {
		return this.typ;
	}

	public void setTyp(Character typ) {
		this.typ = typ;
	}

	public Character getKardinalitaet() {
		return this.kardinalitaet;
	}

	public void setKardinalitaet(Character kardinalitaet) {
		this.kardinalitaet = kardinalitaet;
	}

	public Byte getMetaNeu() {
		return this.metaNeu;
	}

	public void setMetaNeu(Byte metaNeu) {
		this.metaNeu = metaNeu;
	}

	public Integer getMetaVers() {
		return this.metaVers;
	}

	public void setMetaVers(Integer metaVers) {
		this.metaVers = metaVers;
	}

	public Integer getObsoletVers() {
		return this.obsoletVers;
	}

	public void setObsoletVers(Integer obsoletVers) {
		this.obsoletVers = obsoletVers;
	}

	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Date getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Date getLoeschDatum() {
		return this.loeschDatum;
	}

	public void setLoeschDatum(Date loeschDatum) {
		this.loeschDatum = loeschDatum;
	}

	public Byte getImpNeu() {
		return this.impNeu;
	}

	public void setImpNeu(Byte impNeu) {
		this.impNeu = impNeu;
	}

	public String getGuidOrg() {
		return this.guidOrg;
	}

	public void setGuidOrg(String guidOrg) {
		this.guidOrg = guidOrg;
	}

	public Integer getUsn() {
		return this.usn;
	}

	public void setUsn(Integer usn) {
		this.usn = usn;
	}

	public String getErstelltDurch() {
		return this.erstelltDurch;
	}

	public void setErstelltDurch(String erstelltDurch) {
		this.erstelltDurch = erstelltDurch;
	}

	public String getGeloeschtDurch() {
		return this.geloeschtDurch;
	}

	public void setGeloeschtDurch(String geloeschtDurch) {
		this.geloeschtDurch = geloeschtDurch;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof StgMbZielobjRelationId))
			return false;
		StgMbZielobjRelationId castOther = (StgMbZielobjRelationId) other;

		return ((this.getZotId1() == castOther.getZotId1()) || (this
				.getZotId1() != null && castOther.getZotId1() != null && this
				.getZotId1().equals(castOther.getZotId1())))
				&& ((this.getZotId2() == castOther.getZotId2()) || (this
						.getZotId2() != null && castOther.getZotId2() != null && this
						.getZotId2().equals(castOther.getZotId2())))
				&& ((this.getZotImpId() == castOther.getZotImpId()) || (this
						.getZotImpId() != null
						&& castOther.getZotImpId() != null && this
						.getZotImpId().equals(castOther.getZotImpId())))
				&& ((this.getTyp() == castOther.getTyp()) || (this.getTyp() != null
						&& castOther.getTyp() != null && this.getTyp().equals(
						castOther.getTyp())))
				&& ((this.getKardinalitaet() == castOther.getKardinalitaet()) || (this
						.getKardinalitaet() != null
						&& castOther.getKardinalitaet() != null && this
						.getKardinalitaet()
						.equals(castOther.getKardinalitaet())))
				&& ((this.getMetaNeu() == castOther.getMetaNeu()) || (this
						.getMetaNeu() != null && castOther.getMetaNeu() != null && this
						.getMetaNeu().equals(castOther.getMetaNeu())))
				&& ((this.getMetaVers() == castOther.getMetaVers()) || (this
						.getMetaVers() != null
						&& castOther.getMetaVers() != null && this
						.getMetaVers().equals(castOther.getMetaVers())))
				&& ((this.getObsoletVers() == castOther.getObsoletVers()) || (this
						.getObsoletVers() != null
						&& castOther.getObsoletVers() != null && this
						.getObsoletVers().equals(castOther.getObsoletVers())))
				&& ((this.getGuid() == castOther.getGuid()) || (this.getGuid() != null
						&& castOther.getGuid() != null && this.getGuid()
						.equals(castOther.getGuid())))
				&& ((this.getTimestamp() == castOther.getTimestamp()) || (this
						.getTimestamp() != null
						&& castOther.getTimestamp() != null && this
						.getTimestamp().equals(castOther.getTimestamp())))
				&& ((this.getLoeschDatum() == castOther.getLoeschDatum()) || (this
						.getLoeschDatum() != null
						&& castOther.getLoeschDatum() != null && this
						.getLoeschDatum().equals(castOther.getLoeschDatum())))
				&& ((this.getImpNeu() == castOther.getImpNeu()) || (this
						.getImpNeu() != null && castOther.getImpNeu() != null && this
						.getImpNeu().equals(castOther.getImpNeu())))
				&& ((this.getGuidOrg() == castOther.getGuidOrg()) || (this
						.getGuidOrg() != null && castOther.getGuidOrg() != null && this
						.getGuidOrg().equals(castOther.getGuidOrg())))
				&& ((this.getUsn() == castOther.getUsn()) || (this.getUsn() != null
						&& castOther.getUsn() != null && this.getUsn().equals(
						castOther.getUsn())))
				&& ((this.getErstelltDurch() == castOther.getErstelltDurch()) || (this
						.getErstelltDurch() != null
						&& castOther.getErstelltDurch() != null && this
						.getErstelltDurch()
						.equals(castOther.getErstelltDurch())))
				&& ((this.getGeloeschtDurch() == castOther.getGeloeschtDurch()) || (this
						.getGeloeschtDurch() != null
						&& castOther.getGeloeschtDurch() != null && this
						.getGeloeschtDurch().equals(
								castOther.getGeloeschtDurch())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getZotId1() == null ? 0 : this.getZotId1().hashCode());
		result = 37 * result
				+ (getZotId2() == null ? 0 : this.getZotId2().hashCode());
		result = 37 * result
				+ (getZotImpId() == null ? 0 : this.getZotImpId().hashCode());
		result = 37 * result
				+ (getTyp() == null ? 0 : this.getTyp().hashCode());
		result = 37
				* result
				+ (getKardinalitaet() == null ? 0 : this.getKardinalitaet()
						.hashCode());
		result = 37 * result
				+ (getMetaNeu() == null ? 0 : this.getMetaNeu().hashCode());
		result = 37 * result
				+ (getMetaVers() == null ? 0 : this.getMetaVers().hashCode());
		result = 37
				* result
				+ (getObsoletVers() == null ? 0 : this.getObsoletVers()
						.hashCode());
		result = 37 * result
				+ (getGuid() == null ? 0 : this.getGuid().hashCode());
		result = 37 * result
				+ (getTimestamp() == null ? 0 : this.getTimestamp().hashCode());
		result = 37
				* result
				+ (getLoeschDatum() == null ? 0 : this.getLoeschDatum()
						.hashCode());
		result = 37 * result
				+ (getImpNeu() == null ? 0 : this.getImpNeu().hashCode());
		result = 37 * result
				+ (getGuidOrg() == null ? 0 : this.getGuidOrg().hashCode());
		result = 37 * result
				+ (getUsn() == null ? 0 : this.getUsn().hashCode());
		result = 37
				* result
				+ (getErstelltDurch() == null ? 0 : this.getErstelltDurch()
						.hashCode());
		result = 37
				* result
				+ (getGeloeschtDurch() == null ? 0 : this.getGeloeschtDurch()
						.hashCode());
		return result;
	}

}