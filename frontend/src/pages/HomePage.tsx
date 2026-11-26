import { Link, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { StayForm } from '@/components/shared/StayForm'
import { useHotels } from '@/hooks/useHotels'
import { hotelPhoto, heroPhoto } from '@/lib/images'
import { defaultStay, stayToParams } from '@/lib/stay'
import { Skeleton } from '@/components/ui/skeleton'

const perks = [
  { title: 'Best rate promise', text: 'Book direct and always get our lowest price.' },
  { title: 'Flexible stays', text: 'Hold a room for 15 minutes while you check out.' },
  { title: 'Every detail considered', text: 'From check-in to breakfast, handled with care.' },
]

export default function HomePage() {
  const navigate = useNavigate()
  const hotels = useHotels()

  return (
    <>
      <section className="relative">
        <img src={heroPhoto} alt="" className="h-[68vh] min-h-[480px] w-full object-cover" />
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-black/20 to-black/10" />
        <div className="absolute inset-x-0 bottom-0 mx-auto max-w-6xl px-6 pb-12">
          <h1 className="max-w-2xl text-5xl text-white md:text-6xl">Stay somewhere worth remembering.</h1>
          <div className="mt-8 rounded-md bg-background p-4 shadow-xl md:p-5">
            <StayForm initial={defaultStay()} onSubmit={(stay) => navigate(`/hotels?${stayToParams(stay)}`)} />
          </div>
        </div>
      </section>

      <section className="mx-auto mt-20 max-w-6xl px-6">
        <div className="flex items-end justify-between">
          <div>
            <p className="text-xs uppercase tracking-[0.2em] text-primary">Destinations</p>
            <h2 className="mt-2 text-4xl">Our hotels</h2>
          </div>
          <Button variant="outline" render={<Link to="/hotels" />}>
            See all
          </Button>
        </div>
        <div className="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {hotels.isPending
            ? Array.from({ length: 3 }, (_, i) => <Skeleton key={i} className="aspect-[4/5]" />)
            : hotels.data?.slice(0, 6).map((hotel) => (
                <Link key={hotel.id} to={`/hotels/${hotel.id}`} className="group relative block aspect-[4/5] overflow-hidden rounded-md">
                  <img
                    src={hotelPhoto(hotel.id, 700)}
                    alt={hotel.name}
                    loading="lazy"
                    className="size-full object-cover transition-transform duration-500 group-hover:scale-105"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent" />
                  <div className="absolute bottom-0 p-6 text-white">
                    <p className="text-xs uppercase tracking-widest text-white/80">{hotel.address.country}</p>
                    <h3 className="text-2xl">{hotel.name}</h3>
                    <p className="text-sm text-white/80">{hotel.address.city}</p>
                  </div>
                </Link>
              ))}
        </div>
      </section>

      <section className="mx-auto mt-24 grid max-w-6xl gap-10 px-6 md:grid-cols-3">
        {perks.map((perk) => (
          <div key={perk.title} className="border-t pt-6">
            <h3 className="text-xl">{perk.title}</h3>
            <p className="mt-2 text-sm text-muted-foreground">{perk.text}</p>
          </div>
        ))}
      </section>
    </>
  )
}
