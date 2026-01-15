import { Observable } from 'rxjs';

/**
* Handles the generic save/edit subscription for entities.
* * @param request$ - The Observable for the request (create or update).
* @param onSuccess - Function to execute if everything goes well.
* @paramentityName - Name of the entity for the generic error message (e.g., "Software").
* @param conflictMessage - Specific message for error 409 (Duplicate).
*/


export function handleSaveRequest<T>(
  request$: Observable<T>,
  onSuccess: (response: T) => void,
  entityName: string,
  conflictMessage: string
): void {
  request$.subscribe({
    next: (response) => {
      onSuccess(response);
    },
    error: (err) => {
      if (err.status === 409) {
        alert(conflictMessage);
      } else {
        alert(`Error saving ${entityName}: ` + (err.error?.message || 'Unknown error'));
      }
    }
  });
}