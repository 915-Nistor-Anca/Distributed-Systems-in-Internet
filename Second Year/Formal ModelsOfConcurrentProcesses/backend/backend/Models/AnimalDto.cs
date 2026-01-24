namespace backend.Models
{
    public class AnimalDto
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public SpeciesDto Species { get; set; }
        public string Gender { get; set; }
        public DateTime BirthDate { get; set; }
        public OwnerDto Owner { get; set; }
    }
}
