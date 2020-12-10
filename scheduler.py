from backend.contact_tracing.contact_tracing import ContactTracing
from backend.contact_tracing.locations import Locations
max_distance=7
max_time=10000
max_period=1.21e+9
m = ContactTracing()
m.create_contacts(max_distance, max_time)
m = ContactTracing()
m.routine_delete(max_period)
m = Locations()
m.routine_delete(max_period)