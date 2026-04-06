export interface ReclamationDto {
  id: number;
  description: string;
  quantite?: number;
  etat: string;
  dateReclamation?: string;
  nomAuteur?: string;
  prenomAuteur?: string;
  roleAuteur?: string;
  cinAuteur?: string;
  laboratoireId?: number;
  laboratoireNom?: string;
  equipementId?: number;
  equipementNom?: string;
}
