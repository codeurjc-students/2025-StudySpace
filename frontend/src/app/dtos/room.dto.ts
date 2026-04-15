import { SoftwareDTO } from '../dtos/software.dto';
import { CampusDTO } from '../dtos/campus.dto';

export interface RoomDTO {
  id: number;
  name: string;
  capacity: number;
  campus: CampusDTO;
  place: string;
  coordenades: string;
  active: boolean;
  software: SoftwareDTO[];
  imageName?: string;
}
