export interface Page<T> {
  content: T[];          
  totalPages: number;    
  totalElements: number; 
  last: boolean;         
  first: boolean;              
  size: number; 
  number: number; 
  numberOfElements: number;  
  sort: any[];         
}