using backend.Models;

namespace backend.Services
{
    public interface ISpeciesService
    {
        Task<int> AddSpeciesAsync(SpeciesDto species);
        Task UpdateSpeciesAsync(SpeciesDto species);
        Task<SpeciesDto> GetSpeciesByIdAsync(int speciesId);
        Task DeleteSpeciesByIdAsync(int speciesId);
        Task<PagedResult<SpeciesDto>> GetAllSpeciessAsync(int pageNumber, int pageSize);
    }
}
