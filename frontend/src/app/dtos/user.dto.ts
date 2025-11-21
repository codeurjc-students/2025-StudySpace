export interface UserDTO {
    id: number;
    name: string;
    email: string;
    roles: string[];
    
    reservations: any[]; 
}