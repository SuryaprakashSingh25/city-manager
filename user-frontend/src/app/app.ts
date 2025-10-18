import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LoaderService } from './shared/loader.component/loader.service';
import { LoaderComponent } from './shared/loader.component/loader.component';
import { ToastComponent } from "./shared/toast/toast.component";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, LoaderComponent, ToastComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected title = 'user-frontend';
}
