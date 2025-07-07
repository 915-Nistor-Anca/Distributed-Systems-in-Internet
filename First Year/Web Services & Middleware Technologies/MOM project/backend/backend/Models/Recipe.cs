using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace backend.Models
{
    public class Recipe
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public string Name { get; set; }
        public string PreparationSteps { get; set; }
        public string Ingredients { get; set; }
        public bool IsVegetarian { get; set; }
        public DateTime PreparationDate { get; set; }
    }
}
