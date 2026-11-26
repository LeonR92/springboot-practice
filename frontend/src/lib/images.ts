const photos = [
  'photo-1566073771259-6a8506099945',
  'photo-1551882547-ff40c63fe5fa',
  'photo-1542314831-068cd1dbfeeb',
  'photo-1520250497591-112f2f40a3f4',
  'photo-1564501049412-61c2a3083791',
  'photo-1445019980597-93fa8acb246c',
  'photo-1571896349842-33c89424de2d',
  'photo-1582719508461-905c673771fd',
]

const url = (id: string, width: number) => `https://images.unsplash.com/${id}?auto=format&fit=crop&w=${width}&q=70`

export const hotelPhoto = (hotelId: number, width: number, offset = 0) => url(photos[(hotelId + offset) % photos.length], width)

export const heroPhoto = url('photo-1618773928121-c32242e63f39', 2000)
