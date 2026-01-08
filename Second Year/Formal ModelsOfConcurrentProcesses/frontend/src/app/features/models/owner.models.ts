export interface Owner {
    id: number;
    name: string;
    phoneNumber: string;
}

export interface OwnerResult {
    items: Owner[];
}

export type OwnerPagination = {
  pageNumber: number;
  pageSize: number;
};
