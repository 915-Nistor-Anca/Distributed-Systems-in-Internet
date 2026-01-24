import { AfterViewInit, Component, inject, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { OwnerResult } from '../models/owner.models';
import { GET_ALL_OWNERS } from 'src/app/store/actions/owner.actions';
import { SELECT_OWNERS_RESULT } from 'src/app/store/selectors/owner.selectors';
import { MatDialog } from '@angular/material/dialog';
import { AddOwnerDialogComponent } from '../add-owner-dialog/add-owner-dialog.component';

@Component({
  selector: 'app-owners-page',
  templateUrl: './owners-page.component.html',
  styleUrls: ['./owners-page.component.scss'],
})
export class OwnersPageComponent implements AfterViewInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  ownerResult$: Observable<OwnerResult>;
  ownerColumns: string[] = ['name', 'phoneNumber'];
   dialog: MatDialog = inject(MatDialog);

  constructor(private store: Store) {
    this.ownerResult$ = this.store.select(SELECT_OWNERS_RESULT);
  }

  openAddOwnerDialog(): void {
    this.dialog.open(AddOwnerDialogComponent, {
      width: '400px'
    });
  }

  ngAfterViewInit(): void {
    this.paginator.pageIndex = 0;
    this.paginator.pageSize = 5;

    this.loadOwners();

    this.paginator.page.subscribe(() => {
      this.loadOwners();
    });
  }

  private loadOwners(): void {
    this.store.dispatch(
      GET_ALL_OWNERS({
        ownerPagination: {
          pageNumber: this.paginator.pageIndex + 1,
          pageSize: this.paginator.pageSize,
        }
      })
    );
  }
}
