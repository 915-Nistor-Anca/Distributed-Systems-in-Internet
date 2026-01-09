export interface Owner {
    id: number;
    name: string;
    phoneNumber: string;
}

export interface OwnerResult {
    items: Owner[];
    totalCount: number;
}

export type OwnerPagination = {
  pageNumber: number;
  pageSize: number;
};
