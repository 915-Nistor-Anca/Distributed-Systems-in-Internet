using backend.Models;
using Microsoft.EntityFrameworkCore;

namespace backend
{
    public class RestaurantDbContext : DbContext
    {
        public RestaurantDbContext(DbContextOptions<RestaurantDbContext> options)
            : base(options) { }

        public DbSet<Recipe> Recipes { get; set; }
    }
}
