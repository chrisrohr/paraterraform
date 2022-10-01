<template>
  <q-page>

    <div class="row flex flex-center">
      <h3>Upload Page</h3>
    </div>

    <div class="row flex flex-center">
      <q-table
        :rows="data"
        :columns="columns"
        style="width: 70%"
      />
    </div>

  </q-page>
</template>

<script>

export default {
  data() {
    return {
      columns: [
        {
          name: 'name',
          label: 'Name',
          align: 'left',
          field: 'name',
          sortable: true,
        },
        {
          name: 'uploadedAt',
          label: 'Uploaded At',
          align: 'left',
          field: 'uploadedAt',
          sortable: true,
        },
      ],
      data: [],
    };
  },
  mounted() {
    this.getStates();
  },
  methods: {
    getStates() {
      this.$api.get('/states')
        .then((response) => {
          this.data = response.data;
        });
    },
  },
};
</script>
