using backend.Models;
using Microsoft.EntityFrameworkCore;

namespace backend.Repositories
{
    public class OwnerRepository: IOwnerRepository
    {
        private readonly VeterinaryClinicDbContext _context;

        public OwnerRepository(VeterinaryClinicDbContext context)
        {
            _context = context;
        }

        public async Task<Owner> AddOwnerAsync(Owner owner)
        {
            await _context.Owners.AddAsync(owner);
            await _context.SaveChangesAsync();
            return owner;
        }

        public async Task DeleteOwnerByIdAsync(int ownerId)
        {
            var owner = await _context.Owners.FindAsync(ownerId);
            if (owner == null)
            {
                throw new Exception($"Owner with id {ownerId} not found.");
            }
            _context.Owners.Remove(owner);
            await _context.SaveChangesAsync();
        }

        public async Task<ICollection<Owner>> GetAllOwnersAsync(int pageNumber, int pageSize)
        {
            var owners = await _context.Owners.AsNoTracking().Skip((pageNumber - 1)*pageSize).Take(pageSize).ToListAsync();
            return owners;
        }

        public async Task<Owner> GetOwnerByIdAsync(int ownerId)
        {
            var owner = await _context.Owners.AsNoTracking().FirstOrDefaultAsync(x => x.Id == ownerId);
            return owner;
        }

        public async Task UpdateOwnerAsync(Owner owner)
        {
            var existingOwner = await _context.Owners.FindAsync(owner.Id);
            if (existingOwner == null)
            {
                throw new Exception($"Owner with id {owner.Id} not found.");
            }
            _context.Entry(existingOwner).State = EntityState.Detached;
            _context.Owners.Update(owner);
            await _context.SaveChangesAsync();
        }

        public async Task<int> GetTotalNumberOfOwnersAsync()
        {
            var totalNumberOfOwners = await _context.Owners.CountAsync();
            return totalNumberOfOwners;
        }
    }
}
