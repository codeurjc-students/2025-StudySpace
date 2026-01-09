import { SoftwareDTO } from '../dtos/software.dto';

export interface RoomDTO {
    id: number;
    name: string;      
    capacity: number;  
    camp: string;      
    place: string; 
    coordenades: string;   
    active: boolean;
    software: SoftwareDTO[]; 
    imageName?: string;
}