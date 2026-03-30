import PrimeVue from 'primevue/config'
import type { App } from 'vue'
import Avatar from 'primevue/avatar'
import Badge from 'primevue/badge'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import Chip from 'primevue/chip'
import Dialog from 'primevue/dialog'
import Divider from 'primevue/divider'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import MultiSelect from 'primevue/multiselect'
import Password from 'primevue/password'
import ProgressSpinner from 'primevue/progressspinner'
import RadioButton from 'primevue/radiobutton'
import SelectButton from 'primevue/selectbutton'
import Tag from 'primevue/tag'
import Textarea from 'primevue/textarea'
import ToggleButton from 'primevue/togglebutton'
import Tooltip from 'primevue/tooltip'
import Ripple from 'primevue/ripple'

import 'primevue/resources/themes/lara-light-green/theme.css'
import 'primevue/resources/primevue.min.css'
import 'primeicons/primeicons.css'
import 'primeflex/primeflex.css'

const commonComponents = {
  Avatar,
  Badge,
  Button,
  Card,
  Checkbox,
  Chip,
  Dialog,
  Divider,
  Dropdown,
  InputNumber,
  InputText,
  Message,
  MultiSelect,
  Password,
  ProgressSpinner,
  RadioButton,
  SelectButton,
  Tag,
  Textarea,
  ToggleButton
}

export default {
  install(app: App) {
    app.use(PrimeVue)

    for (const [name, component] of Object.entries(commonComponents)) {
      app.component(name, component)
      app.component(`Prime${name}`, component)
    }

    app.directive('tooltip', Tooltip)
    app.directive('ripple', Ripple)
  }
}
