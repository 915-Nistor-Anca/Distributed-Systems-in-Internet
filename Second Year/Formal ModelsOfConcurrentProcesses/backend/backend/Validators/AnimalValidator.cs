using backend.Models;

namespace backend.Validators
{
    public class AnimalValidator
    {
        public static void Validate(Animal animal)
        {
            if (string.IsNullOrWhiteSpace(animal.Name))
                throw new ArgumentException("Name is required.");

            if (string.IsNullOrWhiteSpace(animal.Species))
                throw new ArgumentException("Species is required.");

            if (string.IsNullOrWhiteSpace(animal.Gender))
                throw new ArgumentException("Gender is required.");
            else if (animal.Gender != "M" && animal.Gender != "F")
                throw new ArgumentException("Gender must be 'M' or 'F'.");

            if (animal.BirthDate > DateTime.UtcNow)
                throw new ArgumentException("BirthDate cannot be in the future.");
        }
    }
}
