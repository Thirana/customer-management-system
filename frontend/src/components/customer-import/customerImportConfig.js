export const POLL_INTERVAL_MS = 2000

export const FORMAT_ROWS = [
  {
    column: 'A',
    header: 'Name',
    notes: 'Required',
  },
  {
    column: 'B',
    header: 'Date of Birth',
    notes: 'Required, Excel date or yyyy-MM-dd text',
  },
  {
    column: 'C',
    header: 'NIC Number',
    notes: 'Required',
  },
  {
    column: 'D',
    header: 'Operation',
    notes: 'Optional, CREATE or UPDATE',
  },
]
