import { Component, Input, forwardRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-password-input',
  templateUrl: './password-input.component.html',

  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => PasswordInputComponent),
      multi: true
    }
  ]
})
export class PasswordInputComponent implements ControlValueAccessor {
  
  @Input() placeholder: string = 'Enter password';
  
  // Estado interno
  public value: string = '';
  public visible: boolean = false;
  public isDisabled: boolean = false;

  // Funciones placeholder para el ControlValueAccessor
  onChange = (value: string) => {};
  onTouched = () => {};

  toggleVisibility() {
    this.visible = !this.visible;
  }

  // --- Métodos de ControlValueAccessor ---
  writeValue(value: string): void {
    this.value = value || '';
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.isDisabled = isDisabled;
  }
}