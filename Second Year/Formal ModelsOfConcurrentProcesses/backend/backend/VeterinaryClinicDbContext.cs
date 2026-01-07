using backend.Models;
using Microsoft.EntityFrameworkCore;

namespace backend
{
    public class VeterinaryClinicDbContext : DbContext
    {
        public VeterinaryClinicDbContext(DbContextOptions<VeterinaryClinicDbContext> options)
            : base(options) { }

        public DbSet<Animal> Animals { get; set; }
    }
}
