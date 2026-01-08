using backend.Models;
using Microsoft.EntityFrameworkCore;

namespace backend.Repositories
{
    public class SpeciesRepository: ISpeciesRepository
    {
        private readonly VeterinaryClinicDbContext _context;

        public SpeciesRepository(VeterinaryClinicDbContext context)
        {
            _context = context;
        }

        public async Task<int> AddSpeciesAsync(Species owner)
        {
            await _context.Species.AddAsync(owner);
            await _context.SaveChangesAsync();
            return owner.Id;
        }

        public async Task DeleteSpeciesByIdAsync(int ownerId)
        {
            var owner = await _context.Species.FindAsync(ownerId);
            if (owner == null)
            {
                throw new Exception($"Species with id {ownerId} not found.");
            }
            _context.Species.Remove(owner);
            await _context.SaveChangesAsync();
        }

        public async Task<ICollection<Species>> GetAllSpeciessAsync(int pageNumber, int pageSize)
        {
            var owners = await _context.Species.AsNoTracking().Skip((pageNumber - 1)*pageSize).Take(pageSize).ToListAsync();
            return owners;
        }

        public async Task<Species> GetSpeciesByIdAsync(int ownerId)
        {
            var owner = await _context.Species.AsNoTracking().FirstOrDefaultAsync(x => x.Id == ownerId);
            return owner;
        }

        public async Task UpdateSpeciesAsync(Species owner)
        {
            var existingSpecies = await _context.Species.FindAsync(owner.Id);
            if (existingSpecies == null)
            {
                throw new Exception($"Species with id {owner.Id} not found.");
            }
            _context.Entry(existingSpecies).State = EntityState.Detached;
            _context.Species.Update(owner);
            await _context.SaveChangesAsync();
        }

        public async Task<int> GetTotalNumberOfSpeciessAsync()
        {
            var totalNumberOfSpeciess = await _context.Species.CountAsync();
            return totalNumberOfSpeciess;
        }
    }
}
