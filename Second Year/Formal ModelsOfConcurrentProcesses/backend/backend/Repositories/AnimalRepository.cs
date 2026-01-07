using backend.Models;
using Microsoft.EntityFrameworkCore;

namespace backend.Repositories
{
    public class AnimalRepository: IAnimalRepository
    {
        private readonly VeterinaryClinicDbContext _context;

        public AnimalRepository(VeterinaryClinicDbContext context)
        {
            _context = context;
        }

        public async Task<int> AddAnimalAsync(Animal animal)
        {
            Validators.AnimalValidator.Validate(animal);
            await _context.Animals.AddAsync(animal);
            await _context.SaveChangesAsync();
            return animal.Id;
        }

        public async Task DeleteAnimalByIdAsync(int animalId)
        {
            var animal = await _context.Animals.FindAsync(animalId);
            if (animal == null)
            {
                throw new Exception($"Animal with id {animalId} not found.");
            }
            _context.Animals.Remove(animal);
            await _context.SaveChangesAsync();
        }

        public async Task<ICollection<Animal>> GetAllAnimalsAsync(int pageNumber, int pageSize)
        {
            var animals = await _context.Animals.AsNoTracking().Skip((pageNumber - 1)*pageSize).Take(pageSize).ToListAsync();
            return animals;
        }

        public async Task<Animal> GetAnimalByIdAsync(int animalId)
        {
            var animal = await _context.Animals.AsNoTracking().FirstOrDefaultAsync(x => x.Id == animalId);
            return animal;
        }

        public async Task UpdateAnimalAsync(Animal animal)
        {
            Validators.AnimalValidator.Validate(animal);
            var existingAnimal = await _context.Animals.FindAsync(animal.Id);
            if (existingAnimal == null)
            {
                throw new Exception($"Animal with id {animal.Id} not found.");
            }
            _context.Entry(existingAnimal).State = EntityState.Detached;
            _context.Animals.Update(animal);
            await _context.SaveChangesAsync();
        }

        public async Task<int> GetTotalNumberOfAnimalsAsync()
        {
            var totalNumberOfAnimals = await _context.Animals.CountAsync();
            return totalNumberOfAnimals;
        }
    }
}
