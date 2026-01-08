using backend.Models;

namespace backend.Repositories
{
    public interface ISpeciesRepository
    {
        Task<int> AddSpeciesAsync(Species species);
        Task UpdateSpeciesAsync(Species species);
        Task<Species> GetSpeciesByIdAsync(int speciesId);
        Task DeleteSpeciesByIdAsync(int speciesId);
        Task<ICollection<Species>> GetAllSpeciessAsync(int pageNumber, int pageSize);
        Task<int> GetTotalNumberOfSpeciessAsync();
    }
}
