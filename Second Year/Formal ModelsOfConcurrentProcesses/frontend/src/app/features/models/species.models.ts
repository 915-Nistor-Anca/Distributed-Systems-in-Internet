export interface Species {
    id: number;
    name: string;
    isExotic: boolean;
}

export interface SpeciesResult {
    items: Species[];
}

export type SpeciesPagination= {
    pageNumber: number;
    pageSize: number;
}