import { Component, signal } from '@angular/core';
import { ToastService } from './toast.service';
import { AsyncPipe, CommonModule } from '@angular/common';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [AsyncPipe, CommonModule],
  templateUrl: './toast.component.html',
  styleUrls: ['./toast.component.scss']
})
export class ToastComponent {
  constructor(public toastService: ToastService) {}
}
