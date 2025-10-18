import { CommonModule } from "@angular/common";
import { Component, EventEmitter, Input, Output } from "@angular/core";
import { FormsModule } from "@angular/forms";

@Component({
  selector: "app-status-comment",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './status.comment.html',
  styleUrls: ['./status.comment.scss']
})
export class StatusCommentComponent {
  @Input() statusLabel = '';
  @Output() confirm = new EventEmitter<string>();
  @Output() cancel = new EventEmitter<void>();

  comment = '';

  onCancel() {
    this.cancel.emit();
  }

  onConfirm() {
    this.confirm.emit(this.comment.trim());
  }
}
