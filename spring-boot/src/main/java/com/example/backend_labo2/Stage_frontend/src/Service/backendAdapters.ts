function normalizeDepartement(raw: any) {
  if (!raw && raw !== 0) return undefined

  return {
    ...raw,
    id: Number(raw?.id ?? raw?.departementId ?? 0) || undefined,
    nom: String(raw?.nom ?? raw?.departementNom ?? ''),
    description: raw?.description,
    actif: raw?.actif,
  }
}

function normalizeUser(raw: any) {
  if (!raw && raw !== 0) return undefined

  const departement = raw?.departement || raw?.departementId || raw?.departementNom
    ? normalizeDepartement(raw?.departement ?? {
        id: raw?.departementId,
        nom: raw?.departementNom,
      })
    : undefined

  return {
    ...raw,
    id: Number(raw?.id ?? 0) || undefined,
    nom: String(raw?.nom ?? ''),
    prenom: String(raw?.prenom ?? ''),
    email: raw?.email,
    cin: raw?.cin,
    role: raw?.role,
    departement,
    departementId: raw?.departementId ?? departement?.id,
    departementNom: raw?.departementNom ?? departement?.nom,
    // Alignement backend: on supporte plusieurs variantes de naming
    niveau: raw?.niveau ?? raw?.niveauEtude ?? raw?.niveau_etude ?? raw?.niveauScolaire,
    classe: raw?.classe ?? raw?.classeEtudiant ?? raw?.classe_etudiant ?? raw?.groupe,
    attestationUrl: raw?.attestationUrl ?? raw?.attestation_url,
    attestationOriginalName: raw?.attestationOriginalName ?? raw?.attestation_original_name,
    attestationMimeType: raw?.attestationMimeType ?? raw?.attestation_mime_type,
    attestationSize: raw?.attestationSize ?? raw?.attestation_size,
    attestationVerifiee: raw?.attestationVerifiee ?? raw?.attestation_verifiee,
    active: raw?.active,
    isChefDepartement: raw?.isChefDepartement ?? raw?.chefDepartement,
    chefDepartement: raw?.chefDepartement ?? raw?.isChefDepartement,
  }
}

function normalizeLaboratoireShallow(raw: any) {
  if (!raw && raw !== 0) return undefined

  const departement = raw?.departement || raw?.departementId || raw?.departementNom
    ? normalizeDepartement(raw?.departement ?? {
        id: raw?.departementId,
        nom: raw?.departementNom,
      })
    : undefined

  return {
    ...raw,
    id: Number(raw?.id ?? 0) || undefined,
    nomLabo: String(raw?.nomLabo ?? raw?.nom ?? ''),
    nom: String(raw?.nom ?? raw?.nomLabo ?? ''),
    etatLabo: String(raw?.etatLabo ?? raw?.etat ?? ''),
    departement,
    departementId: raw?.departementId ?? departement?.id,
    equipements: Array.isArray(raw?.equipements) ? raw.equipements.map(normalizeEquipement) : raw?.equipements,
  }
}

export function normalizeEquipement(raw: any) {
  if (!raw && raw !== 0) return raw

  const laboratoire = raw?.laboratoire || raw?.laboratoireId || raw?.laboratoireNom
    ? normalizeLaboratoireShallow(raw?.laboratoire ?? {
        id: raw?.laboratoireId,
        nomLabo: raw?.laboratoireNom,
      })
    : undefined

  return {
    ...raw,
    id: Number(raw?.id ?? 0) || undefined,
    identifiant: raw?.identifiant,
    nom: String(raw?.nom ?? ''),
    caracteristique: raw?.caracteristique ?? raw?.description ?? '',
    quantite: Number(raw?.quantite ?? 0) || 0,
    etat: String(raw?.etat ?? raw?.statut ?? 'FONCTIONNEL').toUpperCase(),
    dateAcquisition: raw?.dateAcquisition ? String(raw.dateAcquisition).split('T')[0] : '',
    imageUrl: raw?.imageUrl ?? raw?.image_url ?? raw?.imageURL ?? raw?.img ?? '',
    laboratoire,
    laboratoireId: raw?.laboratoireId ?? laboratoire?.id,
    laboratoireNom: raw?.laboratoireNom ?? laboratoire?.nomLabo,
    hasReclamationEnCours: Boolean(raw?.hasReclamationEnCours),
  }
}

export function normalizeLaboratoire(raw: any) {
  if (!raw && raw !== 0) return raw

  const normalized = normalizeLaboratoireShallow(raw)
  return {
    ...normalized,
    equipements: Array.isArray(raw?.equipements) ? raw.equipements.map(normalizeEquipement) : normalized.equipements ?? [],
  }
}

export function normalizeReservation(raw: any) {
  if (!raw && raw !== 0) return raw

  const laboratoire = raw?.laboratoire || raw?.laboratoireId || raw?.laboratoireNom
    ? normalizeLaboratoireShallow(raw?.laboratoire ?? {
        id: raw?.laboratoireId,
        nomLabo: raw?.laboratoireNom,
      })
    : undefined

  const etudiant = raw?.etudiant || raw?.demandeur || raw?.user || raw?.utilisateur || raw?.etudiantId
    ? normalizeUser(raw?.etudiant ?? raw?.demandeur ?? raw?.user ?? raw?.utilisateur ?? {
        id: raw?.etudiantId ?? raw?.demandeurId ?? raw?.userId,
        nom: raw?.etudiantNom ?? raw?.nomDemandeur,
        prenom: raw?.etudiantPrenom ?? raw?.prenomDemandeur,
        cin: raw?.etudiantCin ?? raw?.cinDemandeur,
        role: raw?.etudiantRole ?? raw?.roleDemandeur,
        classe: raw?.classeDemandeur,
        departementNom: raw?.departementDemandeur ?? raw?.departementNomDemandeur,
      })
    : undefined

  return {
    ...raw,
    id: Number(raw?.id ?? 0) || undefined,
    dateReservation: raw?.dateReservation ? String(raw.dateReservation).split('T')[0] : '',
    heureDebut: String(raw?.heureDebut ?? ''),
    heureFin: String(raw?.heureFin ?? ''),
    motif: raw?.motif ?? raw?.description ?? '',
    description: raw?.description ?? raw?.motif ?? '',
    statut: String(raw?.statut ?? 'EN_ATTENTE').toUpperCase(),
    motifRefus: raw?.motifRefus,
    laboratoire,
    laboratoireId: raw?.laboratoireId ?? laboratoire?.id,
    laboratoireNom: raw?.laboratoireNom ?? laboratoire?.nomLabo,
    etudiant,
    etudiantId: raw?.etudiantId ?? etudiant?.id,
    etudiantNom: raw?.etudiantNom ?? etudiant?.nom,
    etudiantPrenom: raw?.etudiantPrenom ?? etudiant?.prenom,
    etudiantCin: raw?.etudiantCin ?? etudiant?.cin,
    etudiantRole: raw?.etudiantRole ?? etudiant?.role,
    demandeur: etudiant,
    demandeurId: raw?.demandeurId ?? etudiant?.id,
    nomDemandeur: raw?.nomDemandeur ?? etudiant?.nom,
    prenomDemandeur: raw?.prenomDemandeur ?? etudiant?.prenom,
    cinDemandeur: raw?.cinDemandeur ?? etudiant?.cin,
    roleDemandeur: raw?.roleDemandeur ?? etudiant?.role,
    classeDemandeur: raw?.classeDemandeur ?? etudiant?.classe,
    departementDemandeur: raw?.departementDemandeur ?? etudiant?.departement?.nom,
    departementNomDemandeur: raw?.departementNomDemandeur ?? etudiant?.departement?.nom,
  }
}

export function normalizeReclamation(raw: any) {
  if (!raw && raw !== 0) return raw

  const laboratoire = raw?.laboratoire || raw?.laboratoireId || raw?.laboratoireNom
    ? normalizeLaboratoireShallow(raw?.laboratoire ?? {
        id: raw?.laboratoireId,
        nomLabo: raw?.laboratoireNom,
      })
    : undefined

  const equipement = raw?.equipement || raw?.equipementId || raw?.equipementNom
    ? normalizeEquipement(raw?.equipement ?? {
        id: raw?.equipementId,
        nom: raw?.equipementNom,
        identifiant: raw?.equipementIdentifiant ?? raw?.identifiantEquipement,
        laboratoire,
      })
    : undefined

  const auteur = raw?.auteur || raw?.enseignant || raw?.etudiant || raw?.auteurId
    ? normalizeUser(raw?.auteur ?? raw?.enseignant ?? raw?.etudiant ?? {
        id: raw?.auteurId ?? raw?.etudiantId,
        nom: raw?.nomAuteur,
        prenom: raw?.prenomAuteur,
        cin: raw?.cinAuteur,
        role: raw?.roleAuteur,
      })
    : undefined

  return {
    ...raw,
    id: Number(raw?.id ?? 0) || undefined,
    description: String(raw?.description ?? ''),
    quantite: raw?.quantite,
    priorite: String(raw?.priorite ?? raw?.priority ?? 'MOYENNE').toUpperCase(),
    etat: String(raw?.etat ?? raw?.statut ?? 'NON_TRAITEE').toUpperCase(),
    statut: String(raw?.statut ?? raw?.etat ?? 'NON_TRAITEE').toUpperCase(),
    dateReclamation: raw?.dateReclamation ?? raw?.dateCreation ?? raw?.createdAt,
    motifRefus: raw?.motifRefus,
    laboratoire,
    laboratoireId: raw?.laboratoireId ?? laboratoire?.id,
    laboratoireNom: raw?.laboratoireNom ?? laboratoire?.nomLabo,
    equipement,
    equipementId: raw?.equipementId ?? equipement?.id,
    equipementNom: raw?.equipementNom ?? equipement?.nom,
    equipementIdentifiant: raw?.equipementIdentifiant ?? raw?.identifiantEquipement ?? equipement?.identifiant,
    auteur,
    auteurId: raw?.auteurId ?? auteur?.id,
    nomAuteur: raw?.nomAuteur ?? auteur?.nom,
    prenomAuteur: raw?.prenomAuteur ?? auteur?.prenom,
    cinAuteur: raw?.cinAuteur ?? auteur?.cin,
    roleAuteur: raw?.roleAuteur ?? auteur?.role,
  }
}

export function normalizeList<T>(payload: any, mapper: (value: any) => T): T[] {
  const rows = Array.isArray(payload)
    ? payload
    : Array.isArray(payload?.data)
      ? payload.data
      : Array.isArray(payload?.content)
        ? payload.content
        : []

  return rows.map(mapper)
}
